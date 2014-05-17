package com.fusionx.lightirc.service;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.logging.IRCLoggingManager;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.ui.MainActivity;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.connection.ConnectionManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gnu.trove.map.hash.THashMap;

import static android.support.v4.app.NotificationCompat.Builder;
import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.NotificationUtils.NOTIFICATION_MENTION;
import static com.fusionx.lightirc.util.NotificationUtils.notifyOutOfApp;

public class IRCService extends Service {

    private static final int SERVICE_PRIORITY = 50;

    private static final int SERVICE_ID = 1;

    private final BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateExternalStorageState();
        }
    };

    private final Object mEventHelper = new Object() {
        @Subscribe
        public void onMentioned(final OnChannelMentionEvent event) {
            notifyOutOfApp(IRCService.this, event);
        }

        @Subscribe
        public void onPrefsChanged(final OnPreferencesChangedEvent event) {
            updateLoggingState();
        }
    };

    private final Handler mHandler = new Handler();

    private final IRCBinder mBinder = new IRCBinder();

    private final Map<Server, ServiceEventHelper> mEventHelperMap = new THashMap<>();

    private final Map<Server, EventCache> mEventCache = new HashMap<>();

    private boolean mExternalStorageWriteable = false;

    private IRCLoggingManager mLoggingManager;

    private AppPreferences mAppPreferences;

    private boolean mFirstStart = true;

    private ConnectionManager mConnectionManager;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        onFirstStart();
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);

        return START_STICKY;
    }

    public void removeLoggingHandlerAndEventCache(final Server server) {
        mLoggingManager.removeServerFromManager(server);
        mEventCache.remove(server);
        mEventHelperMap.remove(server);
    }

    public void clearAllEventCaches() {
        for (final EventCache cache : mEventCache.values()) {
            cache.evictAll();
        }
    }

    private void onFirstStart() {
        if (mFirstStart) {
            mAppPreferences = AppPreferences.getAppPreferences();
            mLoggingManager = new IRCLoggingManager(this, mAppPreferences);
            startWatchingExternalStorage();
            getBus().register(mEventHelper, SERVICE_PRIORITY);

            mFirstStart = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister observer status
        stopWatchingExternalStorage();
        getBus().unregister(mEventHelper);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        onFirstStart();
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);
        return mBinder;
    }

    public Server requestConnectionToServer(final ServerConfiguration.Builder builder,
            final Collection<String> ignoreList) {
        final Pair<Boolean, Server> pair = mConnectionManager.requestConnection(builder
                .build(), ignoreList, mHandler);

        final boolean exists = pair.first;
        final Server server = pair.second;

        if (!exists) {
            final ServiceEventHelper serviceEventHelper = new ServiceEventHelper(server);
            mEventHelperMap.put(server, serviceEventHelper);
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
    }

    public void requestReconnectionToServer(final Server server) {
        mConnectionManager.requestReconnection(server);
    }

    public PendingIntent getMainActivityIntent() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    public ServiceEventHelper getEventHelper(final Server server) {
        return mEventHelperMap.get(server);
    }

    public EventCache getEventCache(Server server) {
        return mEventCache.get(server);
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
        final Builder builder = new Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(String.format("%d servers connected",
                mConnectionManager.getServerCount()));
        builder.setContentIntent(getMainActivityIntent());

        return builder.build();
    }

    // Binder which returns this service
    public class IRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}