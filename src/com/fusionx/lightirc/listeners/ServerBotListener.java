package com.fusionx.lightirc.listeners;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NoticeEvent;

import android.app.Activity;
import android.view.View;

import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.runnables.AddTabRunnable;
import com.fusionx.lightirc.runnables.ServerLogRunnable;

public class ServerBotListener extends BotListener {
	
	public ServerBotListener(Activity ac, View vi, PircBotX bo) {
		super(ac, vi, bo);
	}
	
	@Override
	public void onJoin(final JoinEvent<PircBotX> event) throws Exception {
		if (event.getUser().getNick().equals(event.getBot().getNick())) {
			a.runOnUiThread(new ServerLogRunnable("You have joined " + event.getChannel().getName(), v));
			a.runOnUiThread(new AddTabRunnable(((ServerChannelActivity) (a)), bot, event.getChannel().getName()));
		}
	}
	
	@Override
	public void onNotice(final NoticeEvent<PircBotX> event) throws Exception {
		a.runOnUiThread(new ServerLogRunnable(event.getMessage(), v));
	}
	
	@Override
	public void onMotd(final MotdEvent<PircBotX> event) throws Exception {
		a.runOnUiThread(new ServerLogRunnable(event.getMotd(), v));
	}
}