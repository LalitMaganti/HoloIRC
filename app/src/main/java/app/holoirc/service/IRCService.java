package app.holoirc.service;

import com.fusionx.bus.Subscribe;
import app.holoirc.R;
import app.holoirc.event.OnChannelMentionEvent;
import app.holoirc.event.OnPreferencesChangedEvent;
import app.holoirc.event.OnQueryEvent;
import app.holoirc.event.OnServerStatusChanged;
import app.holoirc.event.ServerStopRequestedEvent;
import app.holoirc.logging.IRCLoggingManager;
import app.holoirc.misc.AppPreferences;
import app.holoirc.misc.EventCache;
import app.holoirc.misc.NotificationChannels;
import app.holoirc.ui.MainActivity;
import app.holoirc.util.NotificationUtils;
import app.holoirc.util.SharedPreferencesUtils;
import com.google.common.base.Optional;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Pair;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ConnectionManager;
import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.QueryUser;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.base.ServerConfiguration;
import co.fusionx.relay.internal.base.RelayConnectionManager;
import co.fusionx.relay.parser.UserInputParser;

import static app.holoirc.util.MiscUtils.getBus;
import static app.holoirc.util.NotificationUtils.notifyOutOfApp;

public class IRCService extends Service {

    public static final Map<Server, EventCache[]> mEventCache = new HashMap<>();

    private static final int SERVICE_PRIORITY = 50;

    private static final int SERVICE_ID = 1;
    private static final int WEARABLE_STATUS_ID = 2;

    public static final String ADD_MESSAGE_INTENT = "app.holoirc.add_message";
    public static final String EXTRA_SERVER_NAME = "server_name";
    public static final String EXTRA_CHANNEL_NAME = "channel_name";
    public static final String EXTRA_QUERY_NICK = "query_nick";
    public static final String EXTRA_MESSAGE = "message";

    private static final String DISCONNECT_ALL_INTENT = "app.holoirc.disconnect_all";
    private static final String RECONNECT_ALL_INTENT = "app.holoirc.reconnect_all";

    private final BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateExternalStorageState();
        }
    };

    private final BroadcastReceiver mDisconnectAllReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (final ServiceEventInterceptor interceptor : mEventHelperMap.values()) {
                interceptor.unregister();
            }

            NotificationUtils.cancelMentionNotification(context, null);

            final Set<? extends Server> servers = mConnectionManager.getImmutableServerSet();

            mConnectionManager.requestDisconnectAll();
            for (final Server server : servers) {
                cleanupPostDisconnect(server);
            }
            stop();
        }
    };
    private final BroadcastReceiver mReconnectAllReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (final Server server : mConnectionManager.getImmutableServerSet()) {
                if (server.getStatus() == ConnectionStatus.DISCONNECTED) {
                    mConnectionManager.requestReconnection(server);
                }
            }
        }
    };

    private final Object mEventHelper = new Object() {
        @Subscribe
        public void onMentioned(final OnChannelMentionEvent event) {
            notifyOutOfApp(IRCService.this, event.message, event.user,
                    event.channel, true, event.timestamp);
        }

        @Subscribe
        public void onQueried(final OnQueryEvent event) {
            notifyOutOfApp(IRCService.this, event.message, event.queryUser.getNick(),
                    event.queryUser, false, event.timestamp);
        }

        @Subscribe
        public void onServerStatusChanged(final OnServerStatusChanged event) {
            if (mNotification != null) {
                updateNotification();
            }
        }


        @Subscribe
        public void onPrefsChanged(final OnPreferencesChangedEvent event) {
            updateLoggingState();
        }
    };

    private final IRCBinder mBinder = new IRCBinder();

    private final Map<Server, ServiceEventInterceptor> mEventHelperMap = new HashMap<>();

    private boolean mExternalStorageWriteable = false;

    private IRCLoggingManager mLoggingManager;

    private AppPreferences mAppPreferences;

    private ConnectionManager mConnectionManager;
    private Notification mNotification;

    public static EventCache getEventCache(final Server server, boolean darkBackground) {
        EventCache[] cache = mEventCache.get(server);
        if (cache == null) {
            return null;
        }
        return cache[darkBackground ? 1 : 0];
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (SharedPreferencesUtils.isInitialDatabaseRun(this)) {
            SharedPreferencesUtils.onInitialSetup(this);
        }

        mAppPreferences = AppPreferences.getAppPreferences();
        mLoggingManager = new IRCLoggingManager(mAppPreferences);
        mConnectionManager = RelayConnectionManager.getConnectionManager(mAppPreferences);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();

        getBus().register(mEventHelper, SERVICE_PRIORITY);
        registerReceiver(mDisconnectAllReceiver, new IntentFilter(DISCONNECT_ALL_INTENT));
        registerReceiver(mReconnectAllReceiver, new IntentFilter(RECONNECT_ALL_INTENT));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null && ADD_MESSAGE_INTENT.equals(intent.getAction())) {
            String serverName = intent.getStringExtra(EXTRA_SERVER_NAME);
            String channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME);
            String queryNick = intent.getStringExtra(EXTRA_QUERY_NICK);
            Server server = mConnectionManager.getServerIfExists(serverName);
            CharSequence message = intent.getStringExtra(EXTRA_MESSAGE);
            if (server != null && server.getStatus() == ConnectionStatus.CONNECTED
                    && message != null) {
                if (channelName != null) {
                    Optional<? extends Channel> channel =
                            server.getUserChannelInterface().getChannel(channelName);
                    if (channel.isPresent()) {
                        UserInputParser.onParseChannelMessage(channel.get(), message.toString());
                    }
                } else if (queryNick != null) {
                    Optional<? extends QueryUser> user =
                            server.getUserChannelInterface().getQueryUser(queryNick);
                    if (user.isPresent()) {
                        UserInputParser.onParseUserMessage(user.get(), message.toString());
                    }
                }
            }
        }

        return mNotification != null ? START_STICKY : START_NOT_STICKY;
    }

    public void clearAllEventCaches() {
        for (final EventCache[] cache : mEventCache.values()) {
            cache[0].evictAll();
            cache[1].evictAll();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getBus().unregister(mEventHelper);
        unregisterReceiver(mDisconnectAllReceiver);
        unregisterReceiver(mReconnectAllReceiver);
        unregisterReceiver(mExternalStorageReceiver);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("Known servers:");
        for (Server server : mConnectionManager.getImmutableServerSet()) {
            writer.print("  ");
            writer.print(server.getTitle());
            writer.print(" (");
            writer.print(server.getId());
            writer.print(") - ");
            writer.println(server.getStatus());
        }
        writer.println();
        writer.println("Event caches:");
        for (Map.Entry<Server, EventCache[]> entry : mEventCache.entrySet()) {
            EventCache[] caches = entry.getValue();
            writer.print("  ");
            writer.print(entry.getKey().getId());
            writer.print(": light {");
            writer.print(caches[0].toString());
            writer.print(", size=");
            writer.print(caches[0].size());
            writer.print("}. dark {");
            writer.print(caches[1].toString());
            writer.print(", size=");
            writer.print(caches[1].size());
            writer.println("}");
        }
        writer.println("Logging:");
        writer.print("  started=");
        writer.println(mLoggingManager.isStarted());
        writer.print("  mExternalStorageWriteable=");
        writer.println(mExternalStorageWriteable);
    }

    public Server requestConnectionToServer(final ServerConfiguration.Builder builder) {
        final Pair<Boolean, ? extends Server> pair
                = mConnectionManager.requestConnection(builder.build());

        final boolean exists = pair.first;
        final Server server = pair.second;

        if (!exists) {
            final ServiceEventInterceptor serviceEventInterceptor
                    = new ServiceEventInterceptor(server);
            mEventHelperMap.put(server, serviceEventInterceptor);
            mEventCache.put(server, new EventCache[]{
                new EventCache(this, false), new EventCache(this, true)
            });
            mLoggingManager.addServerToManager(server);
        }
        updateNotification();

        return server;
    }

    public Server getServerIfExists(final ServerConfiguration.Builder builder) {
        return getServerIfExists(builder.getTitle());
    }

    public Server getServerIfExists(final String title) {
        return mConnectionManager.getServerIfExists(title);
    }

    public void requestConnectionStoppage(final Server server) {
        mEventHelperMap.get(server).unregister();

        NotificationUtils.cancelMentionNotification(this, server);

        mConnectionManager.requestStoppageAndRemoval(server.getTitle());
        cleanupPostDisconnect(server);

        if (mConnectionManager.getServerCount() > 0) {
            updateNotification();
        } else {
            stop();
        }
    }

    private void stop() {
        mNotification = null;
        stopForeground(true);
        stopSelf();

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.cancel(WEARABLE_STATUS_ID);
    }

    private void cleanupPostDisconnect(final Server server) {
        getBus().post(new ServerStopRequestedEvent(server));

        mLoggingManager.removeServerFromManager(server);
        mEventCache.remove(server);
        mEventHelperMap.remove(server);
    }

    public void requestReconnectionToServer(final Server server) {
        mConnectionManager.requestReconnection(server);
    }

    public PendingIntent getMainActivityIntent() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    public ServiceEventInterceptor getEventHelper(final Server server) {
        return mEventHelperMap.get(server);
    }

    private void updateExternalStorageState() {
        final String state = Environment.getExternalStorageState();
        mExternalStorageWriteable = state.equals(Environment.MEDIA_MOUNTED);
        updateLoggingState();
    }

    private void updateLoggingState() {
        if (mExternalStorageWriteable && mAppPreferences.isLoggingEnabled()) {
            if (!mLoggingManager.isStarted()) {
                mLoggingManager.startLogging();
            }
        } else {
            if (mLoggingManager.isStarted()) {
                mLoggingManager.stopLogging();
            }
        }
    }

    private void updateNotification() {
        Set<? extends Server> servers = mConnectionManager.getImmutableServerSet();
        List<String> disconnectedServerNames = new ArrayList<>();
        int connectedCount = 0, disconnectedCount = 0;
        int connectingCount = 0, reconnectingCount = 0;

        for (Server server : servers) {
            switch (server.getStatus()) {
                case DISCONNECTED:
                    if (mEventHelperMap.get(server).getLastKnownStatus() != null) {
                        disconnectedServerNames.add(server.getTitle());
                        disconnectedCount++;
                    }
                    break;
                case CONNECTING:
                case REGISTERING:
                    connectingCount++;
                    break;
                case CONNECTED:
                    connectedCount++;
                    break;
                case RECONNECTING:
                    reconnectingCount++;
                    break;
            }
        }

        final int totalCount = disconnectedCount + connectedCount
                + connectingCount + reconnectingCount;
        final StringBuilder publicText = new StringBuilder();

        if (connectedCount > 0) {
            publicText.append(getResources().getQuantityString(
                    R.plurals.server_connection, connectedCount, connectedCount));
        }
        if (connectingCount > 0) {
            if (publicText.length() > 0) {
                publicText.append(", ");
            }
            publicText.append(getResources().getQuantityString(
                    R.plurals.server_connecting, connectingCount, connectingCount));

        }
        if (reconnectingCount > 0) {
            if (publicText.length() > 0) {
                publicText.append(", ");
            }
            publicText.append(getResources().getQuantityString(
                    R.plurals.server_reconnection, reconnectingCount, reconnectingCount));
        }
        if (disconnectedCount > 0) {
            if (publicText.length() > 0) {
                publicText.append(", ");
            }
            publicText.append(getResources().getQuantityString(
                    R.plurals.server_disconnection, disconnectedCount, disconnectedCount));

        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannels.NOTIFICATION_STATUS_UPDATE);
        builder.setColor(getResources().getColor(R.color.colorPrimary));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentIntent(getMainActivityIntent());
        builder.setPriority(disconnectedCount > 0
                ? NotificationCompat.PRIORITY_DEFAULT : NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        builder.setOngoing(true);
        builder.setLocalOnly(true);
        builder.setShowWhen(false);
        builder.setContentText(publicText);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (disconnectedCount > 0) {
            builder.setGroup("serverstatus");
            builder.setGroupSummary(true);
        }

        Notification publicVersion = builder.build();
        final String text;

        if (totalCount == 1) {
            final int formatResId;
            if (connectedCount > 0) {
                formatResId = R.string.notification_connected_title;
            } else if (connectingCount > 0) {
                formatResId = R.string.notification_connecting_title;
            } else if (reconnectingCount > 0) {
                formatResId = R.string.notification_reconnecting_title;
            } else {
                formatResId = R.string.notification_disconnected_title;
            }
            text = getString(formatResId, servers.iterator().next().getId());
        } else {
            text = publicText.toString();
        }

        builder.setContentText(text);
        builder.setTicker(text);
        builder.setPublicVersion(publicVersion);
        builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);

        NotificationCompat.Action reconnectAction = null;

        if (disconnectedCount > 0) {
            final PendingIntent reconnectIntent = PendingIntent.getBroadcast(this, 0,
                    new Intent(RECONNECT_ALL_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
            int reconnectActionResId = disconnectedCount > 1
                    ? R.string.notification_action_reconnect_all
                    : R.string.notification_action_reconnect;
            reconnectAction = new NotificationCompat.Action(
                    R.drawable.ic_refresh_light,
                    getString(reconnectActionResId), reconnectIntent);
            builder.addAction(reconnectAction);
        }

        final PendingIntent intent = PendingIntent.getBroadcast(this, 0,
                new Intent(DISCONNECT_ALL_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
        final int disconnectActionResId;
        if (connectedCount == 0 && connectingCount == 0) {
            disconnectActionResId = totalCount > 1
                    ? R.string.notification_action_close_all : R.string.notification_action_close;
        } else {
            disconnectActionResId = totalCount > 1
                    ? R.string.notification_action_disconnect_all
                    : R.string.notification_action_disconnect;
        }
        builder.addAction(R.drawable.ic_clear_light, getString(disconnectActionResId), intent);

        mNotification = builder.build();
        // make ourself persistent
        startService(new Intent(this, IRCService.class));
        startForeground(SERVICE_ID, mNotification);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);

        if (reconnectAction != null) {
            NotificationCompat.Builder wearableStatusBuilder = new NotificationCompat.Builder(this, NotificationChannels.NOTIFICATION_RECONNECT);
            wearableStatusBuilder.setColor(getResources().getColor(R.color.colorPrimary));
            wearableStatusBuilder.setContentTitle(
                    getString(R.string.notification_reconnect_wear_title));
            wearableStatusBuilder.setContentText(getString(
                    R.string.notification_reconnect_wear_content,
                    TextUtils.join(", ", disconnectedServerNames)));
            wearableStatusBuilder.setSmallIcon(R.drawable.ic_notification_small);
            wearableStatusBuilder.setGroup("serverstatus");

            reconnectAction.icon = R.drawable.ic_refresh_action_wear;
            new NotificationCompat.WearableExtender()
                    .addAction(reconnectAction)
                    .setContentAction(0)
                    .setHintHideIcon(true)
                    .extend(wearableStatusBuilder);

            nm.notify(WEARABLE_STATUS_ID, wearableStatusBuilder.build());
        } else {
            nm.cancel(WEARABLE_STATUS_ID);
        }
    }

    // Binder which returns this service
    public class IRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}
