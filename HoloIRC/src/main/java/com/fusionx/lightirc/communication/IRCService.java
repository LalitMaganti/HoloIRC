package com.fusionx.lightirc.communication;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.connection.ConnectionManager;
import com.fusionx.relay.interfaces.Conversation;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class IRCService extends Service implements EventPriorityHelper.Callback {

    private final Handler mHandler = new Handler();

    private final NewIRCBinder mBinder = new NewIRCBinder();

    private final AppPreferences mAppPreferences = new AppPreferences();

    private final Map<String, EventPriorityHelper> mEventHelperMap = new HashMap<>();

    private ConnectionManager mConnectionManager;

    private Conversation mConversation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);
        return START_STICKY;
    }

    public Server connectToServer(final ServerConfiguration.Builder builder) {
        final Pair<Boolean, Server> pair = mConnectionManager.onConnectionRequested(builder
                .build(), mHandler);

        final boolean exists = pair.first;
        final Server server = pair.second;

        if (!exists) {
            final EventPriorityHelper eventPriorityHelper = new EventPriorityHelper(server,
                    this);
            mEventHelperMap.put(server.getTitle(), eventPriorityHelper);
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
        mConnectionManager.onDisconnectionRequested(server.getTitle());
    }

    @Override
    public Conversation getConversation() {
        return mConversation;
    }

    public void setConversation(final Conversation conversation) {
        mConversation = conversation;
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