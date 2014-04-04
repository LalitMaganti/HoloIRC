package com.fusionx.lightirc.communication;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.connection.ConnectionManager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class IRCService extends Service {

    private final Handler mHandler = new Handler();

    private final NewIRCBinder mBinder = new NewIRCBinder();

    private final AppPreferences mAppPreferences = new AppPreferences();

    private ConnectionManager mConnectionManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public Server connectToServer(final ServerConfiguration.Builder builder) {
        return mConnectionManager.onConnectionRequested(builder.build(), mHandler).second;
    }

    public Server getServerIfExists(final ServerConfiguration.Builder builder) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);
        return mConnectionManager.getServerIfExists(builder.getTitle());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void requestDisconnectionFromServer(Server server) {
        mConnectionManager.onDisconnectionRequested(server.getTitle());
    }

    // Binder which returns this service
    public class NewIRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}