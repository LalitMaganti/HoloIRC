package com.fusionx.lightirc.service;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.SessionStopRequestedEvent;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.event.OnQueryEvent;
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

import co.fusionx.relay.core.ConnectionConfiguration;
import co.fusionx.relay.core.Session;
import co.fusionx.relay.core.SessionConfiguration;
import co.fusionx.relay.core.SessionManager;
import co.fusionx.relay.internal.base.RelaySessionManager;
import co.fusionx.relay.internal.function.FluentIterables;

import static android.support.v4.app.NotificationCompat.Builder;
import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.NotificationUtils.NOTIFICATION_MENTION;
import static com.fusionx.lightirc.util.NotificationUtils.notifyOutOfApp;

public class IRCService extends Service {

    public static final Map<Session, EventCache> sEventCache = new HashMap<>();

    private static final int SERVICE_PRIORITY = 50;

    private static final int SERVICE_ID = 1;

    private static final String DISCONNECT_ALL_INTENT = "com.fusionx.lightirc.disconnect_all";

    private static IRCLoggingManager sLoggingManager;

    private static SessionManager sSessionManager;

    private static Map<Session, ServiceEventInterceptor> sEventHelpers;

    private final BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateExternalStorageState();
        }
    };

    private final BroadcastReceiver mDisconnectAllReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (final ServiceEventInterceptor interceptor : sEventHelpers.values()) {
                interceptor.unregister();
            }

            final NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_MENTION);

            final Set<? extends Session> servers = sSessionManager.sessions();

            sSessionManager.requestDisconnectAll();
            stopForeground(true);

            FluentIterables.forEach(FluentIterable.from(servers),
                    IRCService::cleanupPostDisconnect);
        }
    };

    private final Object mEventHelper = new Object() {
        @Subscribe
        public void onMentioned(final OnChannelMentionEvent event) {
            notifyOutOfApp(IRCService.this, event.connection, event.channel, true);
        }

        @Subscribe
        public void onQueried(final OnQueryEvent event) {
            notifyOutOfApp(IRCService.this, event.connection, event.queryUser, false);
        }

        @Subscribe
        public void onPrefsChanged(final OnPreferencesChangedEvent event) {
            updateLoggingState();
        }
    };

    private final IRCBinder mBinder = new IRCBinder();

    private boolean mExternalStorageWriteable = false;

    private AppPreferences mAppPreferences;

    private boolean mFirstStart = true;

    public static EventCache getEventCache(final Session server) {
        return sEventCache.get(server);
    }

    public static ServiceEventInterceptor getEventHelper(final Session connection) {
        return sEventHelpers.get(connection);
    }

    public static Optional<Session> getConnectionIfExists(final ConnectionConfiguration.Builder
            builder) {
        return getServerIfExists(builder.getTitle());
    }

    public static Optional<Session> getServerIfExists(final String title) {
        return sSessionManager.getConnectionIfExists(title);
    }

    private static void cleanupPostDisconnect(final Session server) {
        getBus().post(new SessionStopRequestedEvent(server));

        sLoggingManager.removeConnectionFromManager(server);
        sEventCache.remove(server);
        sEventHelpers.remove(server);
    }

    public static void requestReconnectionToServer(final Session server) {
        sSessionManager.requestReconnection(server);
    }

    public Session requestConnectionToServer(final ConnectionConfiguration.Builder builder) {
        final SessionConfiguration.Builder session = new SessionConfiguration.Builder();
        session.setConnectionConfiguration(builder.build());
        session.setSettingsProvider(AppPreferences.getAppPreferences());

        final Pair<Boolean, Session> pair = sSessionManager.requestConnection(session.build());

        final boolean exists = pair.first;
        final Session server = pair.second;

        if (!exists) {
            final ServiceEventInterceptor serviceEventInterceptor
                    = new ServiceEventInterceptor(server);
            sEventHelpers.put(server, serviceEventInterceptor);
            sEventCache.put(server, new EventCache(this));
            sLoggingManager.addConnectionToManager(server);
        }
        startForeground(SERVICE_ID, getNotification());
        return server;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        onFirstStart();

        return START_STICKY;
    }

    public void clearAllEventCaches() {
        for (final EventCache cache : sEventCache.values()) {
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
        return mBinder;
    }

    public void requestConnectionStoppage(final Session connection) {
        sEventHelpers.get(connection).unregister();

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_MENTION);

        final boolean finalServer = sSessionManager.requestStoppageAndRemoval(connection
                .getServer().getTitle());
        if (finalServer) {
            stopForeground(true);
        } else {
            startForeground(SERVICE_ID, getNotification());
        }
        cleanupPostDisconnect(connection);
    }

    public PendingIntent getMainActivityIntent() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private void onFirstStart() {
        if (mFirstStart) {
            mAppPreferences = AppPreferences.setupAppPreferences(this);

            sLoggingManager = new IRCLoggingManager(mAppPreferences);
            sSessionManager = RelaySessionManager.createSessionManager();
            sEventHelpers = new HashMap<>();

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
            if (!sLoggingManager.isStarted()) {
                sLoggingManager.startLogging();
            }
        } else {
            if (sLoggingManager.isStarted()) {
                sLoggingManager.stopLogging();
            }
        }
    }

    private Notification getNotification() {
        final Builder builder = new Builder(this);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
        builder.setLargeIcon(icon);
        builder.setContentTitle(getString(R.string.app_name));
        final String text = String.format("%d servers connected", sSessionManager.size());
        builder.setContentText(text);
        builder.setTicker(text);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentIntent(getMainActivityIntent());

        final PendingIntent intent = PendingIntent.getBroadcast(this, 199,
                new Intent(DISCONNECT_ALL_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_clear, "Disconnect all", intent);

        return builder.build();
    }

    // Binder which returns this service
    public class IRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}