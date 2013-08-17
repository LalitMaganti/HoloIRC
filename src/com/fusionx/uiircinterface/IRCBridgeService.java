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

package com.fusionx.uiircinterface;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.irc.connection.ConnectionManager;
import com.fusionx.irc.connection.ConnectionWrapper;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.IRCFragmentActivity;
import com.fusionx.lightirc.activity.MainServerListActivity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

/**
 * A service which acts as a bridge between the pure IRC part of the code and the UI/frontend code
 *
 * @author Lalit Maganti
 */
public class IRCBridgeService extends Service {
    // Binder which returns this service
    public class IRCBinder extends Binder {
        public IRCBridgeService getService() {
            return IRCBridgeService.this;
        }
    }

    @Getter
    private ConnectionManager connectionManager = null;
    private final IRCBinder mBinder = new IRCBinder();
    @Setter(AccessLevel.PUBLIC)
    private String serverDisplayed = null;

    public void connectToServer(final ServerConfiguration.Builder server) {
        if (connectionManager == null) {
            // Means that this that a server is being connected to for the first time
            connectionManager = new ConnectionManager(getApplicationContext());
        }

        setupNotification();

        final ServerConfiguration configuration = server.build();

        MessageSender.getSender(server.getTitle()).initialSetup(getApplicationContext());
        final ConnectionWrapper thread = new ConnectionWrapper(configuration, this);
        connectionManager.put(server.getTitle(), thread);
        thread.start();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupNotification() {
        final Intent intent = new Intent(this, MainServerListActivity.class);
        final Intent intent2 = new Intent(this, IRCBridgeService.class);
        intent2.putExtra("stop", true);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final PendingIntent pIntent2 = PendingIntent.getService(this, 0, intent2, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_holoirc_running))
                .setTicker(getString(R.string.service_holoirc_running))
                        // TODO - change to a proper icon
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        final Notification notification = builder.addAction(android.R.drawable
                .ic_menu_close_clear_cancel,
                getString(R.string.service_disconnect_all), pIntent2).build();

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, notification);
    }

    public void disconnectAll() {
        if (connectionManager != null) {
            connectionManager.disconnectAll();
        }

        stopForeground(true);
    }

    public void disconnectFromServer(final String serverName) {
        final AsyncTask<Void, Void, Void> disconnectTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... strings) {
                final ConnectionWrapper wrapper = connectionManager.get(serverName);
                final Server server = wrapper.getServer();
                final String status = server.getStatus();
                server.setStatus(getString(R.string.status_disconnected));

                if (status.equals(getString(R.string.status_connected))) {
                    wrapper.disconnectFromServer();
                } else if (wrapper.isAlive()) {
                    wrapper.interrupt();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final Void bot) {
                connectionManager.remove(serverName);
                if (connectionManager.isEmpty()) {
                    stopForeground(true);
                }
            }
        };
        disconnectTask.execute();
    }

    @Synchronized
    public void onUnexpectedDisconnect(final String serverName) {
        if (serverDisplayed == null) {
            if (connectionManager.containsKey(serverName)) {
                connectionManager.remove(serverName);
                if (connectionManager.isEmpty()) {
                    stopForeground(true);
                }
            }
        }
    }

    public Server getServer(final String serverName) {
        if (connectionManager != null && connectionManager.get(serverName) != null) {
            return connectionManager.get(serverName).getServer();
        } else {
            return null;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            serverDisplayed = intent.getStringExtra("setBound");
            if (intent.getBooleanExtra("stop", false)) {
                disconnectAll();
                return 0;
            }
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        serverDisplayed = null;

        return true;
    }

    public void mention(final String serverName, final String messageDestination) {
        Notification notification;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_you_mentioned) + " " + messageDestination)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setTicker(getString(R.string.service_you_mentioned) + " " + messageDestination);

        if (!serverName.equals(serverDisplayed)) {
            final Intent mIntent = new Intent(this, IRCFragmentActivity.class);
            //mIntent.putExtra("server", new ServerConfiguration.Builder
            // (getServer(serverName).getConfiguration()));
            mIntent.putExtra("mention", messageDestination);
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addParentStack(IRCFragmentActivity.class);
            taskStackBuilder.addNextIntent(mIntent);
            final PendingIntent pIntent = taskStackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notification = builder.setContentIntent(pIntent).build();
        } else {
            notification = builder.build();
        }

        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(345, notification);
        mNotificationManager.cancel(345);
    }
}