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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.AppUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.UserChannelInterface;
import com.fusionx.lightirc.irc.parser.connection.ServerConnectionParser;
import com.fusionx.lightirc.irc.parser.main.ServerLineParser;
import com.fusionx.lightirc.irc.writers.ServerWriter;
import com.fusionx.lightirc.uiircinterface.MessageSender;
import com.fusionx.lightirc.util.MiscUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Class which carries out all the interesting connection stuff including the inital setting up
 * logic
 *
 * @author Lalit Maganti
 */
class ServerConnection {
    @Getter(AccessLevel.PACKAGE)
    private Server server;
    private final Context mContext;

    private final ServerConfiguration serverConfiguration;
    private Socket mSocket;

    private ServerLineParser parser;

    @Setter(AccessLevel.PACKAGE)
    private boolean disconnectSent = false;

    private int timesToTry;
    private int reconnectAttempts = 0;

    /**
     * Constructor for the object - package local since this object should always be contained
     * only within a {@link ConnectionWrapper} object
     *
     * @param configuration - the ServerConfiguration which should be used to connect to the server
     * @param context       - context for retrieving strings from the res files
     * @param wrapper       - the wrapper object which the Connection is contained by
     */
    ServerConnection(final ServerConfiguration configuration, final Context context,
                     final ConnectionWrapper wrapper) {
        server = new Server(configuration.getTitle(), wrapper, context);
        serverConfiguration = configuration;
        mContext = context;
    }

    /**
     * Method which keeps trying to reconnect to the server the number of times specified and if
     * the user has not explicitly tried to disconnect
     */
    void connectToServer() {
        timesToTry = MiscUtils.getNumberOfReconnectEvents(mContext);
        reconnectAttempts = 0;

        connect();

        final MessageSender sender = MessageSender.getSender(server.getTitle());
        while (!disconnectSent && reconnectAttempts < timesToTry) {
            sender.sendGenericServerEvent(server, "Trying to " +
                    "reconnect to the server in 5 seconds.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // This interrupt will *should* only ever occur if the user explicitly kills
                // reconnection
                sender.sendFinalDisconnection(server, "Disconnected from the server",
                        disconnectSent);
                break;
            }
            connect();
            ++reconnectAttempts;
        }
    }

    /**
     * Called by the connectToServer method ONLY
     */
    private void connect() {
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

            // By sending this line, the server *should* wait until we end the CAP stuff with CAP
            // END
            if (StringUtils.isNotEmpty(serverConfiguration.getSaslPassword()) && StringUtils
                    .isNotEmpty(serverConfiguration.getSaslUsername())) {
                server.getWriter().getSupportedCapabilities();
            }

            if (StringUtils.isNotEmpty(serverConfiguration.getServerPassword())) {
                server.getWriter().sendServerPassword(serverConfiguration.getServerPassword());
            }

            server.getWriter().changeNick(serverConfiguration.getNickStorage().getFirstChoiceNick());
            server.getWriter().sendUser(serverConfiguration.getServerUserName(), "8", "*",
                    StringUtils.isNotEmpty(serverConfiguration.getRealName()) ?
                            serverConfiguration.getRealName() : "HoloIRC");

            final BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket
                    .getInputStream()));
            final String nick = ServerConnectionParser.parseConnect(server, serverConfiguration,
                    reader, mContext);

            // We are connected
            server.setStatus(mContext.getString(R.string.status_connected));
            sender.sendConnected(server, serverConfiguration.getUrl());

            // This nick may well be different from any of the nicks in storage - get the
            // *official* nick from the server itself and use it
            if (nick != null) {
                // Since we are now connected, reset the reconnect attempts
                reconnectAttempts = 0;

                final AppUser user = new AppUser(nick, server.getUserChannelInterface());
                server.setUser(user);

                // Identifies with NickServ if the password exists
                if (StringUtils.isNotEmpty(serverConfiguration.getNickservPassword())) {
                    server.getWriter().sendNickServPassword(serverConfiguration
                            .getNickservPassword());
                }

                // Automatically join the channels specified in the configuration
                for (String channelName : serverConfiguration.getAutoJoinChannels()) {
                    server.getWriter().joinChannel(channelName);
                }

                // Initialise the parser used to parse any lines from the server
                parser = new ServerLineParser(mContext, server);
                // Loops forever until broken
                parser.parseMain(reader);

                // If we have reached this point the connection has been broken - try to
                // reconnect unless the disconnection was requested by the user or we have used
                // all out lives
                if (timesToTry == reconnectAttempts + 1 || disconnectSent) {
                    sender.sendFinalDisconnection(server, "Disconnected from the server",
                            disconnectSent);
                } else {
                    sender.sendRetryPendingServerDisconnection(server,
                            "Disconnected from the server");
                }
            }
        } catch (final IOException ex) {
            // Usually occurs when WiFi/3G is turned off on the device - usually fruitless to try
            // to reconnect but hey ho
            if (timesToTry == reconnectAttempts + 1 || disconnectSent) {
                sender.sendFinalDisconnection(server, ex.getMessage() + "<br/>" + "Disconnected" +
                        " from the server", disconnectSent);
            } else {
                sender.sendRetryPendingServerDisconnection(server, ex.getMessage());
            }
        }
        // We are disconnected :( - close up shop
        server.setStatus(mContext.getString(R.string.status_disconnected));
        server.cleanup();
        closeSocket();
    }

    /**
     * Called when the user explicitly requests a disconnect
     */
    public void disconnectFromServer() {
        disconnectSent = true;
        server.setStatus(mContext.getString(R.string.status_disconnected));
        parser.setDisconnectSent(true);
        server.getWriter().quitServer(MiscUtils.getQuitReason(mContext));
    }

    /**
     * Closes the socket if it is not already closed
     */
    public void closeSocket() {
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}