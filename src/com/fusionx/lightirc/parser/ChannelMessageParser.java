package com.fusionx.lightirc.parser;

import android.content.Context;
import android.content.Intent;

import com.fusionx.lightirc.irc.LightPircBotX;

public class ChannelMessageParser extends IRCMessageParser {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		String channelName = intent.getStringExtra("channel");
		String message = intent.getStringExtra("message");
		String serverName = intent.getStringExtra("serverName");
		LightPircBotX bot = getService().getBot(serverName);

		if (message.startsWith("/")) {
			// TODO parse this string fully
			if (message.startsWith("/join")) {
				String channel = message.replace("/join ", "");
				// TODO - input validation
				bot.joinChannel(channel);
			} else if (message.startsWith("/me")) {
				String action = message.replace("/me ", "");
				// TODO - input validation
				bot.sendAction(channelName, action);
			} else if (message.startsWith("/nick")) {
				String newNick = message.replace("/nick ", "");
				bot.changeNick(newNick);
			} else {
				String bufferMessage = "Unknown command";
				getService().callbackToChannelAndAppend(channelName,
						bufferMessage, serverName);
			}
		} else {
			bot.sendMessage(channelName, message);
			String bufferMessage = bot.getNick() + ": " + message + "\n";
			getService().callbackToChannelAndAppend(channelName, bufferMessage,
					serverName);
		}
	}
}