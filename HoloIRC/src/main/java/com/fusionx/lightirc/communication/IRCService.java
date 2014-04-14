package com.fusionx.lightirc.communication;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.MainActivity;
import com.fusionx.lightirc.util.NotificationUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.connection.ConnectionManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static android.support.v4.app.NotificationCompat.Builder;

public class IRCService extends Service {

    private boolean mRegistered = false;

    private final Object mMentionHelper = new Object() {
        @SuppressWarnings("unused")
        public void onEvent(final OnChannelMentionEvent event) {
            NotificationUtils.notifyOutOfApp(IRCService.this, event);
        }
    };

    private static final int MENTION_PRIORITY = 50;

    private static final int SERVICE_ID = 1;

    private final Handler mHandler = new Handler();

    private final IRCBinder mBinder = new IRCBinder();

    private final AppPreferences mAppPreferences = new AppPreferences();

    private final Map<String, ServiceEventHelper> mEventHelperMap = new HashMap<>();

    private ConnectionManager mConnectionManager;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);

        if (!mRegistered) {
            EventBus.getDefault().register(mMentionHelper, MENTION_PRIORITY);
            mRegistered = true;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);
        return mBinder;
    }

    public Server requestConnectionToServer(final ServerConfiguration.Builder builder,
            final List<String> ignoreList) {
        final Pair<Boolean, Server> pair = mConnectionManager.requestConnection(builder
                .build(), ignoreList, mHandler);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationUtils.NOTIFICATION_MENTION);

        final boolean exists = pair.first;
        final Server server = pair.second;

        if (!exists) {
            final ServiceEventHelper serviceEventHelper = new ServiceEventHelper(server);
            mEventHelperMap.put(server.getTitle(), serviceEventHelper);
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

    public void requestDisconnectionFromServer(final Server server) {
        mEventHelperMap.remove(server.getTitle());

        final boolean finalServer = mConnectionManager
                .requestDisconnectionAndRemoval(server.getTitle());
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

    public ServiceEventHelper getEventHelper(final String title) {
        return mEventHelperMap.get(title);
    }

    public void disconnectAll() {
        mConnectionManager.requestDisconnectAll();
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