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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;

import android.content.Context;
import android.os.Handler;

public class ConnectionWrapper extends Thread {
    private Server mServer;

    private final ServerConnection mConnection;

    private final Handler mHandler;

    public ConnectionWrapper(final ServerConfiguration configuration, final Context context,
            final Handler handler) {
        mServer = new Server(configuration.getTitle(), this, context);
        mConnection = new ServerConnection(configuration, context, mServer);
        mHandler = handler;
    }

    @Override
    public void run() {
        try {
            mConnection.connectToServer();
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
        final String status = mServer.getStatus();

        if (status.equals(context.getString(R.string.status_connected))) {
            mConnection.onDisconnect();
        } else if (isAlive()) {
            interrupt();
            mConnection.closeSocket();
        }
    }

    public Server getServer() {
        return mServer;
    }
}