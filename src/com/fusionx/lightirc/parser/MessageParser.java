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

import com.fusionx.lightirc.service.IRCService;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.managers.ListenerManager;

public class MessageParser {
    private IRCService mService;

    private IRCService getService() {
        return mService;
    }

    public void setService(final IRCService service) {
        mService = service;
    }

    public void channelMessageToParse(final String serverName, final String channelName,
                                      final String message) {
        final PircBotX bot = getService().getBot(serverName);
        final ListenerManager<PircBotX> manager = bot.getConfiguration().getListenerManager();
        final String parsedArray[] = message.split("\\s+");

        if (parsedArray[0].startsWith("/")) {
            // TODO parse this string fully
            if (parsedArray[0].startsWith("/me")) {
                final String action = parsedArray[1];
                // TODO - input validation
                manager.dispatchEvent(new ActionEvent<PircBotX>(bot, bot.getUserBot(),
                        bot.getUserChannelDao().getChannel(channelName), action));
                bot.sendIRC().action(channelName, action);
            } else {
                serverCommandToParse(parsedArray, bot);
            }
        } else {
            manager.dispatchEvent(new MessageEvent<PircBotX>(bot,
                    bot.getUserChannelDao().getChannel(channelName),
                    bot.getUserBot(), message));
            bot.sendIRC().message(channelName, message);
        }
    }

    public void serverMessageToParse(final String serverName, final String message) {
        final PircBotX bot = getService().getBot(serverName);
        final String parsedArray[] = message.split("\\s+");

        if (parsedArray[0].startsWith("/")) {
            serverCommandToParse(parsedArray, bot);
        }
    }

    public void userMessageToParse(final String serverName, final String userNick, final String message) {
        final PircBotX bot = getService().getBot(serverName);
        final ListenerManager<PircBotX> manager = bot.getConfiguration().getListenerManager();
        final String parsedArray[] = message.split("\\s+");

        // TODO parse this string fully
        // TODO - input validation
        if (parsedArray[0].startsWith("/")) {
            if (parsedArray[0].startsWith("/me")) {
                final User user = bot.getUserChannelDao().getUser(userNick);
                String action = message.replace("/me ", "");
                manager.dispatchEvent(new ActionEvent<PircBotX>(bot, user, null, message));
                user.send().action(action);
            } else {
                serverCommandToParse(parsedArray, bot);
            }
        } else {
            final User user = bot.getUserChannelDao().getUser(userNick);
            manager.dispatchEvent(new PrivateMessageEvent<PircBotX>(bot, user, message, true));
            user.send().message(message);
        }
    }

    private void serverCommandToParse(final String[] parsedArray, final PircBotX bot) {
        // TODO parse this string fully
        // TODO - input validation
        final ListenerManager<PircBotX> manager = bot.getConfiguration().getListenerManager();
        final String command = parsedArray[0];

        if (command.equals("/join")) {
            final String channel = parsedArray[1];
            bot.sendIRC().joinChannel(channel);
        } else if (command.equals("/msg")) {
            final String nick = parsedArray[1];
            final String message = ((parsedArray[2] == null) ? "" : parsedArray[2]);

            final User user = bot.getUserChannelDao().getUser(nick);
            manager.dispatchEvent(new PrivateMessageEvent<PircBotX>(bot, user, message, true));
            user.send().message(message);
        } else if (parsedArray[0].startsWith("/nick")) {
            final String newNick = parsedArray[1];
            bot.sendIRC().changeNick(newNick);
        }
    }
}