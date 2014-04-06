package com.fusionx.lightirc.communication;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.MainActivity;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.connection.ConnectionManager;
import com.fusionx.relay.interfaces.Conversation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class IRCService extends Service {

    private static final int SERVICE_ID = 1;

    private final Handler mHandler = new Handler();

    private final NewIRCBinder mBinder = new NewIRCBinder();

    private final AppPreferences mAppPreferences = new AppPreferences();

    private final Map<String, EventPriorityHelper> mEventHelperMap = new HashMap<>();

    private ConnectionManager mConnectionManager;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);
        return START_STICKY;
    }

    public Server connectToServer(final ServerConfiguration.Builder builder) {
        final Pair<Boolean, Server> pair = mConnectionManager.onConnectionRequested(builder
                .build(), mHandler);

        final boolean exists = pair.first;
        final Server server = pair.second;

        if (!exists) {
            final EventPriorityHelper eventPriorityHelper = new EventPriorityHelper(server);
            mEventHelperMap.put(server.getTitle(), eventPriorityHelper);
        }

        if (mConnectionManager.getServerCount() == 1) {
            startForeground(SERVICE_ID, getNotification());
        }

        return server;
    }

    public Server getServerIfExists(final ServerConfiguration.Builder builder) {
        return mConnectionManager.getServerIfExists(builder.getTitle());
    }

    @Override
    public IBinder onBind(final Intent intent) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);
        return mBinder;
    }

    public void requestDisconnectionFromServer(final Server server) {
        mEventHelperMap.remove(server.getTitle());

        final boolean finalServer = mConnectionManager.onDisconnectionRequested(server.getTitle());
        if (finalServer) {
            stopForeground(true);
        }
    }

    private Notification getNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(String.format("%d servers connected",
                mConnectionManager.getServerCount()));
        builder.setContentIntent(getMainActivityIntent());
        return builder.build();
    }

    public PendingIntent getMainActivityIntent() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    public EventPriorityHelper getEventHelper(String title) {
        return mEventHelperMap.get(title);
    }

    // Binder which returns this service
    public class NewIRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}