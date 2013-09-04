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

import android.content.Context;
import android.os.AsyncTask;

import com.fusionx.lightirc.utils.Util;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;

import org.apache.commons.lang3.StringUtils;

public class ServerCommandSender {
    public static void sendJoin(final Server server, final String channelName) {
        final AsyncTask<Void, Void, Void> sendJoin = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().joinChannel(channelName);
                return null;
            }
        };
        sendJoin.execute();
    }

    public static void sendMessageToChannel(final Server server, final String channelName,
                                            final String message) {
        final AsyncTask<Void, Void, Void> sendMessage = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final Channel channel = server.getUserChannelInterface().getChannel(channelName);
                channel.getWriter().sendMessage(message);

                MessageSender.getSender(server.getTitle()).sendMessageToChannel(channel,
                        server.getUser(), message);
                return null;
            }
        };
        sendMessage.execute();
    }

    public static void sendActionToChannel(final Server server, final String channelName,
                                           final String action) {
        final AsyncTask<Void, Void, Void> sendAction = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final Channel channel = server.getUserChannelInterface().getChannel(channelName);
                channel.getWriter().sendAction(action);

                MessageSender.getSender(server.getTitle()).sendChannelAction(channelName,
                        server.getUser(), action);
                return null;
            }
        };
        sendAction.execute();
    }

    public static void sendMessageToUser(final Server server, final String userNick,
                                         final String message) {
        final PrivateMessageUser user = server.getPrivateMessageUser(userNick);
        final AsyncTask<Void, Void, Void> sendMessage = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (StringUtils.isNotEmpty(message)) {
                    user.getWriter().sendMessage(message);
                }
                server.privateMessageSent(user, message, true);
                return null;
            }
        };
        sendMessage.execute();
    }

    public static void sendActionToUser(final Server server, final String userNick,
                                        final String action) {
        final PrivateMessageUser user = server.getPrivateMessageUser(userNick);
        final AsyncTask<Void, Void, Void> sendAction = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (StringUtils.isNotEmpty(action)) {
                    user.getWriter().sendAction(action);
                }
                server.privateActionSent(user, action, true);
                return null;
            }
        };
        sendAction.execute();
    }

    public static void sendNickChange(final Server server, final String newNick) {
        final AsyncTask<Void, Void, Void> changeNick = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().changeNick(newNick);
                return null;
            }
        };
        changeNick.execute();
    }

    public static void sendPart(final Server server, final String channelName,
                                final Context applicationContext) {
        final Channel channel = server.getUserChannelInterface().getChannel(channelName);
        sendPart(channel, applicationContext);
    }

    private static void sendPart(final Channel channel, final Context applicationContext) {
        final AsyncTask<Void, Void, Void> sendPart = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                channel.getWriter().partChannel(Util.getPartReason(applicationContext));
                return null;
            }
        };
        sendPart.execute();
    }

    public static void sendClosePrivateMessage(final Server server, final String nick) {
        sendClosePrivateMessage(server, server.getPrivateMessageUser(nick));
    }

    public static void sendClosePrivateMessage(final Server server, final PrivateMessageUser user) {
        final AsyncTask<Void, Void, Void> sendPart = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getUser().closePrivateMessage(user);
                return null;
            }
        };
        sendPart.execute();
    }

    public static void sendMode(final Server server, final String channelName, final String
            destination, final String mode) {
        final AsyncTask<Void, Void, Void> sendMode = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().sendChannelMode(channelName, destination, mode);
                return null;
            }
        };
        sendMode.execute();
    }

    public static void sendDisconnect(final Server server, final Context context) {
        final AsyncTask<Void, Void, Void> disconnectEvent = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.disconnectFromServer(context);
                return null;
            }
        };
        disconnectEvent.execute();
    }

    public static void sendUnknownEvent(final Server server, final String event) {
        final AsyncTask<Void, Void, Void> unknownEvent = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                MessageSender.getSender(server.getTitle()).switchToServerMessage(event);
                return null;
            }
        };
        unknownEvent.execute();
    }

    public static void sendUserWhois(final Server server, final String nick) {
        final AsyncTask<Void, Void, Void> whois = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().sendWhois(nick);
                return null;
            }
        };
        whois.execute();
    }
}