package com.fusionx.lightirc.runnables;

import org.pircbotx.PircBotX;

import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.fragments.ChannelFragment;

public class AddTabRunnable implements Runnable {
	private final ServerChannelActivity vi;
	private final PircBotX pirc;
	private final String channelName;

	public AddTabRunnable(ServerChannelActivity d, PircBotX r, String cName) {
		vi = d;
		pirc = r;
		channelName = cName;
	}

	@Override
	public void run() {
		int position = vi.mSectionsPagerAdapter.addView(new ChannelFragment(pirc, channelName));
		vi.addTab(position);
	}
}