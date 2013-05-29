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

import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.services.IRCService;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;

public class MessageParser {
    private IRCService mService;

    IRCService getService() {
        return mService;
    }

    public void setService(IRCService service) {
        mService = service;
    }

    public void channelMessageToParse(String serverName, String channelName, String message) {
        final LightBot bot = getService().getBot(serverName);
        if (message != null && message.startsWith("/")) {
            // TODO parse this string fully
            if (message.startsWith("/join")) {
                String channel = message.replace("/join ", "");
                // TODO - input validation
                bot.sendIRC().joinChannel(channel);
            } else if (message.startsWith("/me")) {
                String action = message.replace("/me ", "");
                // TODO - input validation
                bot.sendIRC().action(channelName, action);
                bot.getConfiguration().getListenerManager().dispatchEvent(new ActionEvent(bot, bot.getUserBot(), bot.getUserChannelDao().getChannel(channelName), action));
            } else if (message.startsWith("/nick")) {
                String newNick = message.replace("/nick ", "");
                bot.sendIRC().changeNick(newNick);
            } else {
                //Dispatch event here
            }
        } else {
            bot.sendIRC().message(channelName, message);
            bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(bot, bot.getUserChannelDao().getChannel(channelName), bot.getUserBot(), message));
        }
    }

    public void serverMessageToParse(String serverName, String message) {
        final LightBot bot = getService().getBot(serverName);

        if (message.startsWith("/")) {
            // TODO parse this string fully
            if (message.startsWith("/join")) {
                String channel = message.replace("/join ", "");
                // TODO - input validation
                bot.sendIRC().joinChannel(channel);
            } else {
                String bufferMessage = "Unknown command";
                bot.appendToBuffer(bufferMessage);
            }
        } else {
            String bufferMessage = "Invalid message";
            bot.appendToBuffer(bufferMessage);
        }
    }
}