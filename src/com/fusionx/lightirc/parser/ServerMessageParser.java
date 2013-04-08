package com.fusionx.lightirc.parser;

import com.fusionx.lightirc.irc.LightPircBotX;

import android.content.Context;
import android.content.Intent;

public class ServerMessageParser extends IRCMessageParser {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		String message = intent.getStringExtra("message");
		String serverName = intent.getStringExtra("serverName");
		LightPircBotX bot = getService().getBot(serverName);

		if (message.startsWith("/")) {
			// TODO parse this string fully
			if (message.startsWith("/join")) {
				String channel = message.replace("/join ", "");
				// TODO - input validation
				bot.joinChannel(channel);
			} else {
				String bufferMessage = "Unknown command";
				getService().callbackToServerAndAppend(bufferMessage,
						serverName);
			}
		} else {
			String bufferMessage = "Invalid message";
			getService().callbackToServerAndAppend(bufferMessage, serverName);
		}
	}
}