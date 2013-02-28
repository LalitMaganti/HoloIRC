package com.fusionx.lightirc.listeners;

import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;

public class ConnectionListener extends ListenerAdapter implements Listener {
	@Override
	public void onConnect(final ConnectEvent event) throws Exception {
		String channel = "#testingircandroid";
		event.getBot().joinChannel(channel);
	}
}