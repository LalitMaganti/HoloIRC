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
import android.os.Bundle;

import com.fusionx.irc.AppUser;
import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.irc.UserChannelInterface;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.misc.Utils;
import com.fusionx.irc.parser.ServerConnectionParser;
import com.fusionx.irc.parser.ServerLineParser;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import lombok.AccessLevel;
import lombok.Getter;

class ServerConnection {
    @Getter(AccessLevel.PACKAGE)
    private Server server;
    private final Context mContext;

    private final ServerConfiguration serverConfiguration;
    private Socket mSocket;

    ServerConnection(final ServerConfiguration configuration, final Context context) {
        server = new Server(configuration.getTitle());
        serverConfiguration = configuration;
        mContext = context;
    }

    void connectToServer() throws InterruptedException {
        final MessageSender sender = MessageSender.getSender(server.getTitle());
        try {
            final SSLSocketFactory sslSocketFactory = (SSLSocketFactory)
                    SSLSocketFactory.getDefault();

            mSocket = serverConfiguration.isSsl() ?
                    sslSocketFactory.createSocket(serverConfiguration.getUrl(),
                            serverConfiguration.getPort()) :
                    new Socket(serverConfiguration.getUrl(), serverConfiguration.getPort());

            final OutputStreamWriter writer = new OutputStreamWriter(mSocket.getOutputStream());
            server.setWriter(new ServerWriter(writer));

            server.setStatus(mContext.getString(R.string.status_connecting));

            final UserChannelInterface userChannelInterface = new UserChannelInterface(writer,
                    mContext, server);
            server.setUserChannelInterface(userChannelInterface);

            if (StringUtils.isNotEmpty(serverConfiguration.getServerPassword())) {
                server.getWriter().sendServerPassword(serverConfiguration.getServerPassword());
            }

            server.getWriter().changeNick(serverConfiguration.getNick());
            server.getWriter().sendUser(serverConfiguration.getServerUserName(), "8", "*",
                    StringUtils.isNotEmpty(serverConfiguration.getRealName()) ?
                            serverConfiguration.getRealName() : "HoloIRC");

            final String channel = "#holoirc";

            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream()));
            final String nick = ServerConnectionParser.parseConnect(server.getTitle(), reader);

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerEventType.ServerConnected, String.format(mContext
                    .getString(R.string.parser_connected),
                    serverConfiguration.getUrl()));

            sender.sendServerMessage(event);

            server.setStatus(mContext.getString(R.string.status_connected));

            if (nick != null) {
                final AppUser user = new AppUser(nick, server.getUserChannelInterface());
                server.setUser(user);

                server.getWriter().joinChannel(channel);

                final ServerLineParser parser = new ServerLineParser(mContext, server);
                parser.parseMain(reader);
            } else {
                // An error has occurred - TODO - find out which
            }
        } catch (final IOException ex) {
            // Delay is to allow event to be sent while activity is visible
            Thread.sleep(1000);

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerEventType.Error, ex.getMessage());
            sender.sendServerMessage(event);

            server.setStatus(mContext.getString(R.string.status_disconnected));
        }
    }

    public void disconnectFromServer() {
        server.getWriter().quitServer(Utils.getQuitReason(mContext));
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}