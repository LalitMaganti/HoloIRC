package com.fusionx.lightirc.service;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.event.ServerStopRequestedEvent;
import com.fusionx.lightirc.logging.IRCLoggingManager;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.ui.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import co.fusionx.relay.base.ConnectionManager;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.base.ServerConfiguration;
import co.fusionx.relay.internal.base.RelayConnectionManager;

import static android.support.v4.app.NotificationCompat.Builder;
import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.NotificationUtils.NOTIFICATION_MENTION;
import static com.fusionx.lightirc.util.NotificationUtils.notifyOutOfApp;

public class IRCService extends Service {

    public static final Map<Server, EventCache> mEventCache = new HashMap<>();

    private static final int SERVICE_PRIORITY = 50;

    private static final int SERVICE_ID = 1;

    private static final String DISCONNECT_ALL_INTENT = "com.fusionx.lightirc.disconnect_all";

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

            final NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_MENTION);

            final Set<? extends Server> servers = mConnectionManager.getImmutableServerSet();

            mConnectionManager.requestDisconnectAll();
            stopForeground(true);

            for (final Server server : servers) {
                cleanupPostDisconnect(server);
            }
        }
    };

    private final Object mEventHelper = new Object() {
        @Subscribe
        public void onMentioned(final OnChannelMentionEvent event) {
            notifyOutOfApp(IRCService.this, event.message, event.user, event.channel, true);
        }

        @Subscribe
        public void onQueried(final OnQueryEvent event) {
            notifyOutOfApp(IRCService.this, event.message, event.queryUser.getNick(),
                    event.queryUser, false);
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

    private boolean mFirstStart = true;

    private ConnectionManager mConnectionManager;

    public static EventCache getEventCache(final Server server) {
        return mEventCache.get(server);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        onFirstStart();
        mConnectionManager = RelayConnectionManager.getConnectionManager(mAppPreferences);

        return START_STICKY;
    }

    public void clearAllEventCaches() {
        for (final EventCache cache : mEventCache.values()) {
            cache.evictAll();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister observer status
        stopWatchingExternalStorage();
        getBus().unregister(mEventHelper);
        unregisterReceiver(mDisconnectAllReceiver);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        onFirstStart();
        mConnectionManager = RelayConnectionManager.getConnectionManager(mAppPreferences);
        return mBinder;
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
            mEventCache.put(server, new EventCache(this));
            mLoggingManager.addServerToManager(server);
        }
        startForeground(SERVICE_ID, getNotification());
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

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_MENTION);

        final boolean finalServer = mConnectionManager.requestStoppageAndRemoval(server.getTitle());
        if (finalServer) {
            stopForeground(true);
        } else {
            startForeground(SERVICE_ID, getNotification());
        }
        cleanupPostDisconnect(server);
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

    private void onFirstStart() {
        if (mFirstStart) {
            mAppPreferences = AppPreferences.getAppPreferences();
            mLoggingManager = new IRCLoggingManager(mAppPreferences);
            startWatchingExternalStorage();
            getBus().register(mEventHelper, SERVICE_PRIORITY);
            registerReceiver(mDisconnectAllReceiver, new IntentFilter(DISCONNECT_ALL_INTENT));

            mFirstStart = false;
        }
    }

    private void startWatchingExternalStorage() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }

    private void stopWatchingExternalStorage() {
        unregisterReceiver(mExternalStorageReceiver);
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

    private Notification getNotification() {
        int connectionCount = mConnectionManager.getServerCount();
        final Builder builder = new Builder(this);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
        builder.setLargeIcon(icon);
        builder.setContentTitle(getString(R.string.app_name));
        final String text;
        if (connectionCount == 1) {
            String server = mConnectionManager.getImmutableServerSet().iterator().next().getId();
            text = getString(R.string.parser_connected, server);
        } else {
            text = getResources().getQuantityString(R.plurals.server_connection,
                    connectionCount, connectionCount);
        }
        builder.setContentText(text);
        builder.setTicker(text);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentIntent(getMainActivityIntent());

        final PendingIntent intent = PendingIntent.getBroadcast(this, 199,
                new Intent(DISCONNECT_ALL_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
        int disconnectActionResId = connectionCount > 1
                ? R.string.notification_action_disconnect_all
                : R.string.notification_action_disconnect;
        builder.addAction(R.drawable.ic_clear_light, getString(disconnectActionResId), intent);

        return builder.build();
    }

    // Binder which returns this service
    public class IRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}