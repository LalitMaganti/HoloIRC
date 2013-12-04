/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.communication;

import com.fusionx.androidirclibrary.Server;
import com.fusionx.androidirclibrary.ServerConfiguration;
import com.fusionx.androidirclibrary.communication.MessageSender;
import com.fusionx.androidirclibrary.connection.ConnectionManager;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.ServerListActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

/**
 * A service which acts as a bridge between the pure IRC part of the code and the UI/frontend code
 *
 * @author Lalit Maganti
 */
public class IRCService extends Service {

    private final IRCBinder mBinder = new IRCBinder();

    private final Handler mAdapterHandler = new Handler(Looper.getMainLooper());

    private ConnectionManager mConnectionManager = null;

    private final EventResponses mResponses = new EventResponses(this);

    private final AppPreferences mAppPreferences = new AppPreferences();

    public Server connectToServer(final ServerConfiguration.Builder builder) {
        mConnectionManager = ConnectionManager.getConnectionManager(mResponses, mAppPreferences);

        final ServerConfiguration configuration = builder.build();

        final MessageSender sender = MessageSender.getSender(builder.getTitle());
        sender.getBus().register(this);

        final Server server = mConnectionManager.onConnectionRequested(configuration,
                mAdapterHandler);
        updateNotification();

        return server;
    }

    private void updateNotification() {
        final String text = String.format(getResources().getQuantityString(R.plurals
                .server_connection, mConnectionManager.getConnectedServerCount()),
                mConnectionManager.getConnectedServerCount());
        final Intent intent = new Intent(this, ServerListActivity.class);
        final Intent intent2 = new Intent(this, IRCService.class);
        intent2.putExtra("stop", true);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final PendingIntent pIntent2 = PendingIntent.getService(this, 0, intent2, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name)).setContentText(text).setTicker
                        (text)
                        // TODO - change to a proper icon
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        final Notification notification = builder.addAction(android.R.drawable
                .ic_menu_close_clear_cancel, getString(R.string.service_disconnect_all),
                pIntent2).build();

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, notification);
    }

    public void disconnectAll() {
        synchronized (mBinder) {
            final AsyncTask<Void, Void, Void> disconnectAll = new AsyncTask<Void, Void,
                    Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mConnectionManager.disconnectAll();
                    return null;
                }
            };
            disconnectAll.execute();
        }
        stopForeground(true);

        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        stopSelf();
    }

    public void removeServerFromManager(final String serverName) {
        synchronized (mBinder) {
            MessageSender.getSender(serverName).getBus().unregister(this);
            if (mConnectionManager.onDisconnectionRequested(serverName)) {
                stopForeground(true);
            } else {
                updateNotification();
                final NotificationManager mNotificationManager = (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(345);
            }
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("stop", false)) {
                disconnectAll();
                return 0;
            }
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return true;
    }

    // Binder which returns this service
    public class IRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}