package com.fusionx.lightirc.listeners;

import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NoticeEvent;

import com.fusionx.lightirc.misc.LightPircBotX;

public class ServerListener extends IRCListener {
	@Override
	public void onNotice(final NoticeEvent<LightPircBotX> event) {
		callbackToServerAndAppend(event.getMessage() + "\n", event.getBot()
				.getTitle());
	}

	@Override
	public void onMotd(final MotdEvent<LightPircBotX> event) {
		callbackToServerAndAppend(event.getMotd() + "\n", event.getBot()
				.getTitle());
	}

	private void callbackToServerAndAppend(final String message,
			final String serverName) {
		getService().getBot(serverName).mServerBuffer += message;

		tryPostServer(new Runnable() {
			public void run() {
				getService().getServerCallback().writeToTextView(message);
			}
		});
	}
}