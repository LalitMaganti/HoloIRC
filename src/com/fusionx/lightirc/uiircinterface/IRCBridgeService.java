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

package com.fusionx.lightirc.uiircinterface;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.connection.ConnectionManager;
import com.fusionx.lightirc.irc.connection.ConnectionWrapper;
import com.fusionx.lightirc.ui.MainServerListActivity;

import lombok.AccessLevel;
import lombok.Setter;

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

    private ConnectionManager connectionManager = null;
    private final IRCBinder mBinder = new IRCBinder();
    @Setter(AccessLevel.PUBLIC)
    private String serverDisplayed = null;

    public void connectToServer(final ServerConfiguration.Builder server) {
        if (connectionManager == null) {
            // Means that this that a server is being connected to for the first time
            connectionManager = new ConnectionManager(this);
        }

        final ServerConfiguration configuration = server.build();

        MessageSender.getSender(server.getTitle()).initialSetup(this);
        final ConnectionWrapper thread = new ConnectionWrapper(configuration, this);
        connectionManager.put(server.getTitle(), thread);

        updateNotification();

        thread.start();
    }

    private void updateNotification() {
        final String text = String.format(getResources().getQuantityString(R.plurals
                .server_connection, connectionManager.size()), connectionManager.size());
        final Intent intent = new Intent(this, MainServerListActivity.class);
        final Intent intent2 = new Intent(this, IRCBridgeService.class);
        intent2.putExtra("stop", true);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final PendingIntent pIntent2 = PendingIntent.getService(this, 0, intent2, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name)).setContentText(text).setTicker(text)
                        // TODO - change to a proper icon
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        final Notification notification = builder.addAction(android.R.drawable
                .ic_menu_close_clear_cancel, getString(R.string.service_disconnect_all), pIntent2).build();

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, notification);
    }

    public void disconnectAll() {
        if (connectionManager != null) {
            connectionManager.disconnectAll();
        }

        if (serverDisplayed != null) {
            final Intent intent = new Intent(this,
                    MainServerListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            serverDisplayed = null;
        }

        stopForeground(true);
    }

    public void onDisconnect(final String serverName) {
        if (connectionManager.containsKey(serverName)) {
            connectionManager.remove(serverName);
            if (connectionManager.isEmpty()) {
                stopForeground(true);
            } else {
                updateNotification();
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
}