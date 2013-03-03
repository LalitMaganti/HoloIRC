package com.fusionx.lightirc.listeners;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;

import android.app.Activity;
import android.view.View;

import com.fusionx.lightirc.runnables.ChannelLogRunnable;

public class ChannelListener extends BotListener {
	public ChannelListener(final Activity ac, final View vi, final PircBotX bo) {
		super(ac, vi, bo);
	}

	@Override
	public void onMessage(MessageEvent<PircBotX> event) throws Exception {
		String newLine = event.getUser().getNick() + ": " + event.getMessage();
		a.runOnUiThread(new ChannelLogRunnable(newLine, v));
	}
}