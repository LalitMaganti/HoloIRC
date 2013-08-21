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

package com.fusionx.irc.connection;

import android.content.Context;
import android.os.Looper;

import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.lightirc.R;

import lombok.Getter;

public class ConnectionWrapper extends Thread {
    @Getter
    private Server server;
    private final ServerConnection connection;

    public ConnectionWrapper(final ServerConfiguration configuration, final Context context) {
        connection = new ServerConnection(configuration, context);
        server = connection.getServer();
    }

    @Override
    public void run() {
        Looper.prepare();
        try {
            connection.connectToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromServer(final Context context) {
        final Server server = getServer();
        final String status = server.getStatus();
        server.setStatus(context.getString(R.string.status_disconnected));

        if (status.equals(context.getString(R.string.status_connected))) {
            connection.disconnectFromServer();
        } else if (isAlive()) {
            interrupt();
        }
    }
}