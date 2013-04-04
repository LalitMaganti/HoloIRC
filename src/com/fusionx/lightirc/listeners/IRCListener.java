package com.fusionx.lightirc.listeners;

import java.util.HashMap;

import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

import com.fusionx.lightirc.callbacks.ServerCallbacks;
import com.fusionx.lightirc.misc.LightPircBotX;

import android.os.Handler;

public abstract class IRCListener extends ListenerAdapter<LightPircBotX> implements
		Listener<LightPircBotX> {
	final Handler mHandler = new Handler();
	public ServerCallbacks mServerCallbacks = null;
	public HashMap<String, LightPircBotX> mServerObjects = new HashMap<String, LightPircBotX>();
	
	protected void tryPostServer(final Runnable run) {
		if (mServerCallbacks != null) {
			mHandler.post(run);
		}
	}
}