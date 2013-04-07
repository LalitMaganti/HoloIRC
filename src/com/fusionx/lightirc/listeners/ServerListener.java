package com.fusionx.lightirc.listeners;

import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NoticeEvent;

import com.fusionx.lightirc.irc.LightPircBotX;

public class ServerListener extends IRCListener {
	@Override
	public void onNotice(final NoticeEvent<LightPircBotX> event) {
		getService().callbackToServerAndAppend(event.getMessage() + "\n", event.getBot()
				.getTitle());
	}

	@Override
	public void onMotd(final MotdEvent<LightPircBotX> event) {
		getService().callbackToServerAndAppend(event.getMotd() + "\n", event.getBot()
				.getTitle());
	}
}