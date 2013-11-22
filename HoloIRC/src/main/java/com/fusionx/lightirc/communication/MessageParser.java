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

import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.util.MiscUtils;

import android.content.Context;

import java.util.ArrayList;

/**
 * This entire class needs full parsing
 */
public class MessageParser {

    public static void channelMessageToParse(final Context context, final Server server,
            final String channelName, final String message) {
        final ArrayList<String> parsedArray = MiscUtils.splitRawLine(message, false);
        final String command = parsedArray.remove(0);

        if (command.startsWith("/")) {
            if (command.equals("/me")) {
                final String action = MiscUtils.convertArrayListToString(parsedArray);
                ServerCommandSender.sendActionToChannel(server, channelName, action);
            } else if (command.equals("/part") || command.equals("/p")) {
                if (parsedArray.size() == 0) {
                    ServerCommandSender.sendPart(server, channelName,
                            context.getApplicationContext());
                } else {
                    sendUnknownEvent(server, message);
                }
            } else if (command.equals("/mode")) {
                if (parsedArray.size() == 2) {
                    ServerCommandSender.sendMode(server, channelName, parsedArray.get(0),
                            parsedArray.get(1));
                } else {
                    sendUnknownEvent(server, message);
                }
            } else {
                serverCommandToParse(context, server, message);
            }
        } else {
            ServerCommandSender.sendMessageToChannel(server, channelName, message);
        }
    }

    public static void userMessageToParse(final Context context, final Server server,
            final String userNick, final String message) {
        final ArrayList<String> parsedArray = MiscUtils.splitRawLine(message, false);
        final String command = parsedArray.remove(0);

        if (command.startsWith("/")) {
            if (command.equals("/me")) {
                final String action = MiscUtils.convertArrayListToString(parsedArray);
                ServerCommandSender.sendActionToUser(server, userNick, action);

            } else if (command.equals("/close") || command.equals("/c")) {
                if (parsedArray.size() == 0) {
                    ServerCommandSender.sendClosePrivateMessage(server,
                            server.getPrivateMessageUser(userNick));
                } else {
                    sendUnknownEvent(server, message);
                }
            } else {
                serverCommandToParse(context, server, message);

            }
        } else {
            ServerCommandSender.sendMessageToUser(server, userNick, message);
        }
    }

    public static void serverMessageToParse(final Context context, final Server server,
            final String message) {
        if (message.startsWith("/")) {
            serverCommandToParse(context, server, message);
        } else {
            sendUnknownEvent(server, message);
        }
    }

    private static void serverCommandToParse(final Context context, final Server server,
            final String rawLine) {
        final ArrayList<String> parsedArray = MiscUtils.splitRawLine(rawLine, false);
        final String command = parsedArray.remove(0);

        if (command.equals("/join") || command.equals("/j")) {
            if (parsedArray.size() == 1) {
                final String channelName = parsedArray.get(0);
                ServerCommandSender.sendJoin(server, channelName);
            } else {
                sendUnknownEvent(server, rawLine);
            }

        } else if (command.equals("/msg")) {
            if (parsedArray.size() >= 1) {
                final String nick = parsedArray.remove(0);
                final String message = parsedArray.size() >= 1 ?
                        MiscUtils.convertArrayListToString(parsedArray) : "";
                ServerCommandSender.sendMessageToUser(server, nick, message);
            } else {
                sendUnknownEvent(server, rawLine);
            }

        } else if (command.equals("/nick")) {
            if (parsedArray.size() == 1) {
                final String newNick = parsedArray.get(0);
                ServerCommandSender.sendNickChange(server, newNick);
            } else {
                sendUnknownEvent(server, rawLine);
            }

        } else if (command.equals("/quit")) {
            if (parsedArray.size() == 0) {
                ServerCommandSender.sendDisconnect(server, context);
            } else {
                sendUnknownEvent(server, rawLine);
            }

        } else if (command.equals("/whois")) {
            if (parsedArray.size() == 1) {
                ServerCommandSender.sendUserWhois(server, parsedArray.get(0));
            } else {
                sendUnknownEvent(server, rawLine);
            }

        } else {
            sendUnknownEvent(server, rawLine);
        }
    }

    private static void sendUnknownEvent(final Server server, final String rawLine) {
        ServerCommandSender.sendUnknownEvent(server, rawLine + " is not a valid command");
    }
}