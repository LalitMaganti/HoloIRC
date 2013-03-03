package com.fusionx.lightirc.listeners;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

import android.app.Activity;
import android.view.View;

public abstract class BotListener extends ListenerAdapter<PircBotX> implements Listener<PircBotX> {
	final Activity a;
	final View v;
	final PircBotX bot;
	
	public BotListener(final Activity ac, final View vi, final PircBotX bo) {
		a = ac;
		v = vi;
		bot = bo;
	}
}