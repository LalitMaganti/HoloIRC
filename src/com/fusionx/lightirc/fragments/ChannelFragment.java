package com.fusionx.lightirc.fragments;

import org.pircbotx.PircBotX;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.listeners.ChannelEditTextKeyListener;
import com.fusionx.lightirc.listeners.ChannelListener;
import com.fusionx.lightirc.runnables.ChannelLogRunnable;

public class ChannelFragment extends IRCFragment {
	private PircBotX bo;

	public ChannelFragment(PircBotX bot, String channelName) {
		bo = bot;
		tabTitle = channelName;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);

		getActivity().runOnUiThread(
				new ChannelLogRunnable(bo.getNick() + " entered the room",
						rootView));

		final ChannelListener e = new ChannelListener(getActivity(), rootView,
				bo);
		bo.getListenerManager().addListener(e);

		TextView textview = (TextView) rootView.findViewById(R.id.editText1);
		textview.setOnKeyListener(new ChannelEditTextKeyListener(bo, tabTitle,
				rootView, getActivity()));
		return rootView;
	}
}