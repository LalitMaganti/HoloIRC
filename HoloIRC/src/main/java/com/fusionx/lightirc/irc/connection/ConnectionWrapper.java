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

package com.fusionx.lightirc.irc.connection;

import android.content.Context;
import android.os.Handler;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;

import lombok.Getter;

public class ConnectionWrapper extends Thread {
    @Getter
    private Server server;
    private final ServerConnection connection;
    private final Handler mHandler;

    public ConnectionWrapper(final ServerConfiguration configuration, final Context context,
                             final Handler handler) {
        server = new Server(configuration.getTitle(), this, context);
        connection = new ServerConnection(configuration, context, server);
        mHandler = handler;
    }

    @Override
    public void run() {
        try {
            connection.connectToServer();
        } catch (final Exception ex) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    public void disconnectFromServer(final Context context) {
        final String status = server.getStatus();

        if (status.equals(context.getString(R.string.status_connected))) {
            connection.onDisconnect();
        } else if (isAlive()) {
            connection.setDisconnectSent(true);
            interrupt();
        }
    }
}