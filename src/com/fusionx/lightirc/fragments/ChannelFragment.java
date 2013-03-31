/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fusionx.lightirc.R;

public class ChannelFragment extends IRCFragment implements OnKeyListener {
	String nick;
	String serverName;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);

		tabTitle = getArguments().getString("channel");
		nick = getArguments().getString("nick");
		serverName = getArguments().getString("serverName");
		String buffer = getArguments().getString("buffer");

		if (buffer != null) {
			writeRawToTextView(buffer, rootView);
		} else {
			writeToTextView(nick + " entered the room", rootView);
		}

		TextView textview = (TextView) rootView.findViewById(R.id.editText1);
		textview.setOnKeyListener(this);

		IntentFilter filter = new IntentFilter(
				"com.fusionx.lightirc.MESSAGE_FROM_CHANNEL");
		getActivity().registerReceiver(mChannelReciever, filter);

		return rootView;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		EditText t = (EditText) v;

		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == KeyEvent.KEYCODE_ENTER)
				&& !t.getText().toString().equals("\n")
				&& !t.getText().toString().equals("")) {
			// TODO - need to parse this string
			Intent intent = new Intent();
			intent.setAction("com.fusionx.lightirc.MESSAGE_TO_CHANNEL");
			intent.putExtra("channel", tabTitle);
			intent.putExtra("serverName", serverName);
			intent.putExtra("message", t.getText().toString());
			getActivity().sendBroadcast(intent);

			writeToTextView(nick + ": " + t.getText().toString());

			// Hacky way to clear but keep the focus on the EditText
			t.getText().clear();
			t.setSelection(0);

			return true;
		}

		return false;
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mChannelReciever);
		super.onDestroy();
	}

	BroadcastReceiver mChannelReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String channel = intent.getStringExtra("channel");
			if (channel.equals(tabTitle)) {
				if (intent.getAction().equals(
						"com.fusionx.lightirc.MESSAGE_FROM_CHANNEL")) {
					String message = intent.getStringExtra("message");
					writeToTextView(message);
				}
			}
		}
	};
}
