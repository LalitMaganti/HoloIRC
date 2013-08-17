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

package com.fusionx.lightirc.parser;

import android.content.Context;

import com.fusionx.Utils;
import com.fusionx.irc.Server;
import com.fusionx.uiircinterface.ServerCommandSender;

import java.util.ArrayList;

/**
 * This entire class needs input validation and full parsing
 */
public class MessageParser {
    public static void channelMessageToParse(final Context applicationContext, final Server server,
                                             final String channelName, final String message) {
        final ArrayList<String> parsedArray = Utils.splitLineBySpaces(message);
        final String command = parsedArray.remove(0);

        if (command.startsWith("/")) {
            switch (command) {
                case "/me":
                    final String action = Utils.convertArrayListToString(parsedArray);
                    ServerCommandSender.sendActionToChannel(server, channelName, action);
                    break;
                case "/part":
                case "/p":
                    ServerCommandSender.sendPart(server, channelName, applicationContext);
                    break;
                default:
                    serverCommandToParse(server, message);
                    break;
            }
        } else {
            ServerCommandSender.sendMessageToChannel(server, channelName, message);
        }
    }

    public static void serverMessageToParse(final Server bot, final String message) {
        if (message.startsWith("/")) {
            serverCommandToParse(bot, message);
        } else {
            ServerCommandSender.sendUnknownEvent(bot, message);
        }
    }

    public static void userMessageToParse(final Server server, final String userNick,
                                          final String message) {
        final ArrayList<String> parsedArray = Utils.splitLineBySpaces(message);
        final String command = parsedArray.remove(0);

        if (command.startsWith("/")) {
            switch (command) {
                case "/me":
                    final String action = Utils.convertArrayListToString(parsedArray);
                    ServerCommandSender.sendActionToUser(server, userNick, action);
                    break;
                case "/close":
                case "/c":
                    ServerCommandSender.sendClosePrivateMessage(server,
                            server.getUserChannelInterface().getUser(userNick));
                    break;
                default:
                    serverCommandToParse(server, message);
                    break;
            }
        } else {
            ServerCommandSender.sendMessageToUser(server, userNick, message);
        }
    }

    private static void serverCommandToParse(final Server server, final String rawLine) {
        final ArrayList<String> parsedArray = Utils.splitLineBySpaces(rawLine);
        final String command = parsedArray.remove(0);

        switch (command) {
            case "/join":
            case "/j":
                final String channelName = parsedArray.get(0);
                ServerCommandSender.sendJoin(server, channelName);
                break;
            case "/msg":
                if (parsedArray.size() > 1) {
                    final String nick = parsedArray.remove(0);
                    final String message = parsedArray.size() >= 1 ?
                            Utils.convertArrayListToString(parsedArray) : "";
                    ServerCommandSender.sendMessageToUser(server, nick, message);
                } else {
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
                }
                break;
            case "/nick":
                final String newNick = parsedArray.get(0);
                ServerCommandSender.sendNickChange(server, newNick);
                break;
            default:
                    ServerCommandSender.sendUnknownEvent(server, rawLine);
        }
    }
}