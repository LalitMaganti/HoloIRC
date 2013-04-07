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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.callbacks.ServerCallback;
import com.fusionx.lightirc.misc.LightPircBotX;
import com.fusionx.lightirc.services.IRCService;
import com.fusionx.lightirc.services.IRCService.IRCBinder;
import android.widget.*;

public class ServerFragment extends IRCFragment implements OnKeyListener, ServerCallback {
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Bundle b = getArguments();
		setTitle(b.getString("serverName"));

		// TODO - Hacky way to reset title correctly
		((ServerChannelActivity) getActivity())
				.updateTabTitle(this, getTitle());

		final Intent service = new Intent(getActivity(), IRCService.class);
		service.putExtra("server", true);
		getActivity().startService(service);
		getActivity().bindService(service, mConnection, 0);

		super.onCreate(savedInstanceState);
	}

	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder binder) {
			final IRCService service = ((IRCBinder) binder).getService();
			final LightPircBotX light = service.getBot(getTitle());

			service.setServerCallback(ServerFragment.this);

			if (light.isStarted()) {
				writeToTextView(light.mServerBuffer);
				for (final String channelName : light.getChannelBuffers()
						.keySet()) {
					onNewChannelJoined(channelName, light.getNick(), light
							.getChannelBuffers().get(channelName));
				}
			} else {
				service.connectToServer(getTitle());
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			// This should never happen
		}
	};

	private final ServiceConnection mDisconnectConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder binder) {
			IRCService service = ((IRCBinder) binder).getService();
			service.disconnectFromServer(getTitle());
			getActivity().unbindService(this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			// This should never happen
		}
	};

	@Override
	public void onDestroy() {
		getActivity().unbindService(mConnection);
		super.onDestroy();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);

		TextView textview = (TextView) rootView.findViewById(R.id.editText1);
		textview.setOnKeyListener(this);

		return rootView;
	}
	
	@Override
	public void onNewChannelJoined(final String channelName, final String nick,
			final String buffer) {
		final ChannelFragment channel = new ChannelFragment();
		final Bundle b = new Bundle();
		b.putString("channel", channelName);
		b.putString("nick", nick);
		b.putString("serverName", getTitle());
		b.putString("buffer", buffer);
		channel.setArguments(b);

		final ServerChannelActivity parentActivity = ((ServerChannelActivity) getActivity());
		parentActivity.addChannelFragment(channel, channelName);
	}

	public void disconnect() {
		final Intent service = new Intent(getActivity(), IRCService.class);
		getActivity().bindService(service, mDisconnectConnection, 0);
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		EditText editText = (EditText) view;

		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == KeyEvent.KEYCODE_ENTER)
				&& !editText.getText().toString().equals("\n")
				&& !editText.getText().toString().isEmpty()) {
			Intent intent = new Intent();
			intent.setAction("com.fusionx.lightirc.SERVER_MESSAGE_TO_PARSE");
			intent.putExtra("serverName", getTitle());
			intent.putExtra("message", editText.getText().toString());
			getActivity().sendBroadcast(intent);

			// Hacky way to clear but keep the focus on the EditText
			// Doesn't seem to work anymore :/
			editText.getText().clear();
			editText.setSelection(0);

			return true;
		}
		return false;
	}
}
