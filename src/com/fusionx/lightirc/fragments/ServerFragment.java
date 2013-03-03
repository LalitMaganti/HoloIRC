package com.fusionx.lightirc.fragments;

import org.pircbotx.PircBotX;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.ServerObject;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.background.BackgroundIRCConnector;
import com.fusionx.lightirc.listeners.ServerBotListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ServerFragment extends IRCFragment {

	final PircBotX bot = new PircBotX();

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);
		
		Bundle b = getArguments();
		final ServerObject s = (ServerObject) b.getSerializable("serverObject");

		bot.setName(s.nick);
		// To-do - setup option for this
		bot.setAutoNickChange(true);
		
		// Hacky way to reset title correctly
		((ServerChannelActivity) getActivity()).updateTabTitle(this, s.title);
		
		final ServerBotListener listener = new ServerBotListener(getActivity(), rootView, bot);
		bot.getListenerManager().addListener(listener);

		final BackgroundIRCConnector connectInBackground = new BackgroundIRCConnector(bot);
		connectInBackground.execute(s, null, null);
		
		return rootView;
	}
}