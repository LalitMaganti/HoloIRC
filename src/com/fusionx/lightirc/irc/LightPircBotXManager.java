package com.fusionx.lightirc.irc;

import java.util.HashMap;

public class LightPircBotXManager extends HashMap<String, LightPircBotX> {
	private static final long serialVersionUID = 1L;

	public void disconnectAll() {
		for (LightPircBotX bot : values()) {
			bot.disconnect();
		}
	}
}
