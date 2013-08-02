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

package com.fusionx.ircinterface.connection;

import android.content.Context;
import com.fusionx.ircinterface.Server;
import com.fusionx.ircinterface.ServerConfiguration;
import com.fusionx.ircinterface.misc.BroadcastSender;
import lombok.Getter;

public class ConnectionWrapper extends Thread {
    @Getter
    private Server server;
    private final ServerConnection connection;

    public ConnectionWrapper(final ServerConfiguration configuration, final Context context,
                             final BroadcastSender broadcastSender) {
        connection = new ServerConnection(configuration, context, broadcastSender);
        server = connection.getServer();
    }

    @Override
    public void run() {
        try {
            connection.connectToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}