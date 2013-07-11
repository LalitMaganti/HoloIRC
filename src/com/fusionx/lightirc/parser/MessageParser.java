/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.parser;

import android.content.Context;
import org.pircbotx.PircBotX;

public class MessageParser {
    /**
        This entire class needs input validation and full parsing
    */

    public static void channelMessageToParse(final Context applicationContext, final PircBotX bot,
                                             final String channelName, final String message) {
        final String parsedArray[] = message.split("\\s+");
        final String command = parsedArray[0];

        if (command.startsWith("/")) {
            if (command.equals("/me")) {
                final String action = message.replace("/me ", "");
                ServerCommunicator.sendActionToChannel(bot, channelName, action);
            } else if (command.equals("/part")) {
                ServerCommunicator.sendPart(bot, channelName, applicationContext);
            } else {
                serverCommandToParse(bot, message);
            }
        } else {
            ServerCommunicator.sendMessageToChannel(bot, channelName, message);
        }
    }

    public static void serverMessageToParse(final PircBotX bot, final String message) {
        final String parsedArray[] = message.split("\\s+");
        final String command = parsedArray[0];

        if (command.startsWith("/")) {
            serverCommandToParse(bot, message);
        } else {
            ServerCommunicator.sendUnknownEvent(bot, message);
        }
    }

    public static void userMessageToParse(final PircBotX bot, final String userNick, final String message) {
        final String parsedArray[] = message.split("\\s+");
        final String command = parsedArray[0];

        if (command.startsWith("/")) {
            if (command.startsWith("/me")) {
                final String action = message.replace("/me ", "");
                ServerCommunicator.sendActionToUser(bot, userNick, action);
            } else {
                serverCommandToParse(bot, message);
            }
        } else {
            ServerCommunicator.sendMessageToUser(bot, userNick, message);
        }
    }

    private static void serverCommandToParse(final PircBotX bot, final String rawLine) {
        final String parsedArray[] = rawLine.split("\\s+");
        final String command = parsedArray[0];

        if (command.equals("/join") || command.equals("/j")) {
            final String channelName = parsedArray[1];
            ServerCommunicator.sendJoin(bot, channelName);
        } else if (command.equals("/msg")) {
            if (parsedArray.length > 1) {
                final String nick = parsedArray[1];
                final String message = ((parsedArray.length == 2) ? "" : rawLine.replace("/msg ", "")
                        .replace(nick + " ", ""));
                ServerCommunicator.sendMessageToUser(bot, nick, message);
            } else {
                ServerCommunicator.sendUnknownEvent(bot, rawLine);
            }
        } else if (command.startsWith("/nick")) {
            final String newNick = parsedArray[1];
            ServerCommunicator.sendNickChange(bot, newNick);
        } else {
            ServerCommunicator.sendUnknownEvent(bot, rawLine);
        }
    }
}