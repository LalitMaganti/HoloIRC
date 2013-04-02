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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.services.IRCService;
import com.fusionx.lightirc.services.IRCService.IRCBinder;

public class ChannelFragment extends IRCFragment implements OnKeyListener,
		ChannelCallbacks {
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
		
		writeToTextView(buffer, rootView);

		TextView textview = (TextView) rootView.findViewById(R.id.editText1);
		textview.setOnKeyListener(this);

		Intent service = new Intent(getActivity(), IRCService.class);
		service.putExtra("channel", tabTitle);
		getActivity().bindService(service, mConnection, 0);

		return rootView;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			((IRCBinder) service).getService().setChannelCallbacks(
					ChannelFragment.this, tabTitle);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

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

			// Hacky way to clear but keep the focus on the EditText
			t.getText().clear();
			t.setSelection(0);

			return true;
		}

		return false;
	}

	@Override
	public void onDestroy() {
		getActivity().unbindService(mConnection);
		super.onDestroy();
	}

	@Override
	public void onChannelWriteNeeded(String message) {
		writeToTextView(message);
	}
}
