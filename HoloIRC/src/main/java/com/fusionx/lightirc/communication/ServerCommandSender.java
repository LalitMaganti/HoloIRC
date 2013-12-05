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

package com.fusionx.lightirc.communication;

import com.fusionx.androidirclibrary.Channel;
import com.fusionx.androidirclibrary.PrivateMessageUser;
import com.fusionx.androidirclibrary.Server;
import com.fusionx.androidirclibrary.event.ActionEvent;
import com.fusionx.androidirclibrary.event.JoinEvent;
import com.fusionx.androidirclibrary.event.MessageEvent;
import com.fusionx.androidirclibrary.event.NickChangeEvent;
import com.fusionx.androidirclibrary.event.PartEvent;
import com.fusionx.lightirc.misc.AppPreferences;

import android.content.Context;
import android.os.AsyncTask;

public class ServerCommandSender {

    public static void sendJoin(final Server server, final String channelName) {
        server.getFrontEndToServerBus().post(new JoinEvent(channelName));
    }

    public static void sendMessageToChannel(final Server server, final String channelName,
            final String message) {
        server.getFrontEndToServerBus().post(new MessageEvent(channelName, message));

        final Channel channel = server.getUserChannelInterface().getChannel(channelName);
        server.getServerToFrontEndBus().sendMessageToChannel(server
                .getUser(), channel, server.getUser(), message);
    }

    public static void sendActionToChannel(final Server server, final String channelName,
            final String action) {
        server.getFrontEndToServerBus().post(new ActionEvent(channelName, action));

        final Channel channel = server.getUserChannelInterface().getChannel(channelName);
        server.getServerToFrontEndBus().sendChannelAction(server
                .getUser(), channel, server.getUser(), action);
    }

    public static void sendMessageToUser(final Server server, final String userNick,
            final String message) {
        /*final PrivateMessageUser user = server.getPrivateMessageUser(userNick);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (StringUtils.isNotEmpty(message)) {
                    user.getWriter().sendMessage(message);
                }
                server.onPrivateMessage(user, message, true);
                return null;
            }
        }.execute();*/
    }

    public static void sendActionToUser(final Server server, final String userNick,
            final String action) {
        /*final PrivateMessageUser user = server.getPrivateMessageUser(userNick);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (StringUtils.isNotEmpty(action)) {
                    user.getWriter().sendAction(action);
                }
                server.onPrivateAction(user, action, true);
                return null;
            }
        }.execute();*/
    }

    public static void sendNickChange(final Server server, final String newNick) {
        server.getFrontEndToServerBus().post(new NickChangeEvent(server.getUser().getNick(),
                newNick));
    }

    public static void sendPart(final Server server, final String channelName) {
        server.getFrontEndToServerBus().post(new PartEvent(channelName, AppPreferences.partReason));
    }

    public static void sendClosePrivateMessage(final Server server, final String nick) {
        sendClosePrivateMessage(server, server.getPrivateMessageUser(nick));
    }

    public static void sendClosePrivateMessage(final Server server, final PrivateMessageUser user) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getUser().closePrivateMessage(user);
                return null;
            }
        }.execute();
    }

    public static void sendMode(final Server server, final String channelName, final String
            destination, final String mode) {
        /*new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().sendChannelMode(channelName, destination, mode);
                return null;
            }
        }.execute();*/
    }

    public static void sendDisconnect(final Server server, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.disconnectFromServer();
                return null;
            }
        }.execute();
    }

    public static void sendUnknownEvent(final Server server, final String event) {
        /*new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                MessageSender.getSender(server.getTitle()).sendSwitchToServerEvent(server, event);
                return null;
            }
        }.execute();*/
    }

    public static void sendUserWhois(final Server server, final String nick) {
        /*new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().sendWhois(nick);
                return null;
            }
        }.execute();*/
    }

    public static void sendRawLine(final Server server, final String rawLine) {
        /*new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().sendRawLineToServer(rawLine);
                return null;
            }
        }.execute();*/
    }
}