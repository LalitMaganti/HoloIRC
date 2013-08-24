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

package com.fusionx.uiircinterface;

import android.content.Context;

import com.fusionx.common.Utils;
import com.fusionx.irc.Server;

import java.util.ArrayList;

/**
 * This entire class needs full parsing
 */
public class MessageParser {
    public static void channelMessageToParse(final Context context, final Server server,
                                             final String channelName, final String message) {
        final ArrayList<String> parsedArray = Utils.splitRawLine(message, false);
        final String command = parsedArray.remove(0);

        if (command.startsWith("/")) {
            switch (command) {
                case "/me":
                    final String action = Utils.convertArrayListToString(parsedArray);
                    ServerCommandSender.sendActionToChannel(server, channelName, action);
                    break;
                case "/part":
                case "/p":
                    if (parsedArray.size() == 0) {
                        ServerCommandSender.sendPart(server, channelName,
                                context.getApplicationContext());
                    } else {
                        ServerCommandSender.sendUnknownEvent(server, message);
                    }
                    break;
                case "/mode":
                    if (parsedArray.size() == 2) {
                        ServerCommandSender.sendMode(server, channelName, parsedArray.get(0),
                                parsedArray.get(1));
                    } else {
                        ServerCommandSender.sendUnknownEvent(server, message);
                    }
                    break;
                default:
                    serverCommandToParse(context, server, message);
                    break;
            }
        } else {
            ServerCommandSender.sendMessageToChannel(server, channelName, message);
        }
    }

    public static void userMessageToParse(final Context context, final Server server,
                                          final String userNick, final String message) {
        final ArrayList<String> parsedArray = Utils.splitRawLine(message, false);
        final String command = parsedArray.remove(0);

        if (command.startsWith("/")) {
            switch (command) {
                case "/me":
                    final String action = Utils.convertArrayListToString(parsedArray);
                    ServerCommandSender.sendActionToUser(server, userNick, action);
                    break;
                case "/close":
                case "/c":
                    if (parsedArray.size() == 0) {
                        ServerCommandSender.sendClosePrivateMessage(server,
                                server.getPrivateMessageUser(userNick));
                    } else {
                        ServerCommandSender.sendUnknownEvent(server, message);
                    }
                    break;
                default:
                    serverCommandToParse(context, server, message);
                    break;
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
            ServerCommandSender.sendUnknownEvent(server, message);
        }
    }

    private static void serverCommandToParse(final Context context, final Server server,
                                             final String rawLine) {
        final ArrayList<String> parsedArray = Utils.splitRawLine(rawLine, false);
        final String command = parsedArray.remove(0);

        switch (command) {
            case "/join":
            case "/j":
                if (parsedArray.size() == 1) {
                    final String channelName = parsedArray.get(0);
                    ServerCommandSender.sendJoin(server, channelName);
                } else {
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
                }
                break;
            case "/msg":
                if (parsedArray.size() >= 1) {
                    final String nick = parsedArray.remove(0);
                    final String message = parsedArray.size() >= 1 ?
                            Utils.convertArrayListToString(parsedArray) : "";
                    ServerCommandSender.sendMessageToUser(server, nick, message);
                } else {
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
                }
                break;
            case "/nick":
                if (parsedArray.size() == 1) {
                    final String newNick = parsedArray.get(0);
                    ServerCommandSender.sendNickChange(server, newNick);
                } else {
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
                }
                break;
            case "/quit":
                if (parsedArray.size() == 0) {
                    ServerCommandSender.sendDisconnect(server, context);
                } else {
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
                }
                break;
            default:
                ServerCommandSender.sendUnknownEvent(server, rawLine);
        }
    }
}