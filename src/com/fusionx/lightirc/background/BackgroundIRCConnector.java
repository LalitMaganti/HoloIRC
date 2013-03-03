package com.fusionx.lightirc.background;

import org.pircbotx.PircBotX;

import android.os.AsyncTask;

import com.fusionx.lightirc.misc.ServerObject;

public class BackgroundIRCConnector extends AsyncTask<ServerObject, Void, Void> {
	
	final PircBotX bot;
	
	public BackgroundIRCConnector(final PircBotX bo) {
		bot = bo;
	}
	
	@Override
	protected Void doInBackground(final ServerObject... serverDetails) {
		try {
			bot.connect(serverDetails[0].url);
			bot.joinChannel(serverDetails[0].autoJoinChannels[0]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}