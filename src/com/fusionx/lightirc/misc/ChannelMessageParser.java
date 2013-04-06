package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.services.IRCService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ChannelMessageParser extends BroadcastReceiver {
	private IRCService mService;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent.getAction().equals(
				"com.fusionx.lightirc.CHANNEL_MESSAGE_TO_PARSE")) {
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
				} else {
					String bufferMessage = "Unknown command";
					getService().callbackToChannelAndAppend(channelName,
							bufferMessage, serverName);
				}
			} else {
				bot.sendMessage(channelName, message);
				String bufferMessage = bot.getNick() + ": " + message + "\n";
				getService().callbackToChannelAndAppend(channelName,
						bufferMessage, serverName);
			}
		}
	}

	public IRCService getService() {
		return mService;
	}

	public void setService(IRCService service) {
		mService = service;
	}
}