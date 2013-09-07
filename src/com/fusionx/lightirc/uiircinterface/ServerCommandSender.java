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

import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.util.MiscUtils;

import org.apache.commons.lang3.StringUtils;

public class ServerCommandSender {
    public static void sendJoin(final Server server, final String channelName) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                server.getWriter().joinChannel(channelName);
            }
        });
    }

    public static void sendMessageToChannel(final Server server, final String channelName,
                                            final String message) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                final Channel channel = server.getUserChannelInterface().getChannel(channelName);
                channel.getWriter().sendMessage(message);

                MessageSender.getSender(server.getTitle()).sendMessageToChannel(server,
                        server.getUser().getNick(), channel, server.getUser().getPrettyNick
                        (channel), message);
            }
        });
    }

    public static void sendActionToChannel(final Server server, final String channelName,
                                           final String action) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                final Channel channel = server.getUserChannelInterface().getChannel(channelName);
                channel.getWriter().sendAction(action);

                MessageSender.getSender(server.getTitle()).sendChannelAction(server, server
                        .getUser().getNick(), channel,  server.getUser(), action);
            }
        });
    }

    public static void sendMessageToUser(final Server server, final String userNick,
                                         final String message) {
        final PrivateMessageUser user = server.getPrivateMessageUser(userNick);
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isNotEmpty(message)) {
                    user.getWriter().sendMessage(message);
                }
                server.privateMessageSent(user, message, true);
            }
        });
    }

    public static void sendActionToUser(final Server server, final String userNick,
                                        final String action) {
        final PrivateMessageUser user = server.getPrivateMessageUser(userNick);
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isNotEmpty(action)) {
                    user.getWriter().sendAction(action);
                }
                server.privateActionSent(user, action, true);
            }
        });
    }

    public static void sendNickChange(final Server server, final String newNick) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                server.getWriter().changeNick(newNick);
            }
        });
    }

    public static void sendPart(final Server server, final String channelName,
                                final Context applicationContext) {
        final Channel channel = server.getUserChannelInterface().getChannel(channelName);
        sendPart(server, channel, applicationContext);
    }

    private static void sendPart(final Server server, final Channel channel,
                                 final Context applicationContext) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                channel.getWriter().partChannel(MiscUtils.getPartReason(applicationContext));
            }
        });
    }

    public static void sendClosePrivateMessage(final Server server, final String nick) {
        sendClosePrivateMessage(server, server.getPrivateMessageUser(nick));
    }

    public static void sendClosePrivateMessage(final Server server, final PrivateMessageUser user) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                server.getUser().closePrivateMessage(user);
            }
        });
    }

    public static void sendMode(final Server server, final String channelName, final String
            destination, final String mode) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                server.getWriter().sendChannelMode(channelName, destination, mode);
            }
        });
    }

    public static void sendDisconnect(final Server server, final Context context) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                server.disconnectFromServer(context);
            }
        });
    }

    public static void sendUnknownEvent(final Server server, final String event) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                MessageSender.getSender(server.getTitle()).switchToServerMessage(server, event);
            }
        });
    }

    public static void sendUserWhois(final Server server, final String nick) {
        server.getHandler().post(new Runnable() {
            @Override
            public void run() {
                server.getWriter().sendWhois(nick);
            }
        });
    }
}