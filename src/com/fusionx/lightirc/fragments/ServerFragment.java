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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.LightPircBotX;
import com.fusionx.lightirc.services.IRCService;
import com.fusionx.lightirc.services.IRCService.IRCBinder;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.callbacks.ServerCallbacks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ServerFragment extends IRCFragment implements ServerCallbacks {
	String serverName;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Bundle b = getArguments();
		serverName = b.getString("serverName");
		
		// Hacky way to reset title correctly
		((ServerChannelActivity) getActivity()).updateTabTitle(this, serverName);
		
		Intent service = new Intent(getActivity(), IRCService.class);
		service.putExtra("server", true);
		getActivity().startService(service);
		getActivity().bindService(service, mConnection, 0);
			
		super.onCreate(savedInstanceState);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LightPircBotX light = ((IRCBinder) service).getService().getBot(serverName);
			((IRCBinder) service).getService().setServerCallbacks(ServerFragment.this);
			if(light.mIsStarted) {
				writeToTextView(light.mServerBuffer);
				for(String s : light.mChannelBuffers.keySet()) {
					ChannelFragment channel = new ChannelFragment();
					Bundle bu = new Bundle();
					bu.putString("channel", s);
					bu.putString("nick", light.mNick);
					bu.putString("serverName", serverName);
					bu.putString("buffer", light.mChannelBuffers.get(s));
					channel.setArguments(bu);

					int position = ((ServerChannelActivity) getActivity()).mSectionsPagerAdapter
							.addView(channel);
					((ServerChannelActivity) getActivity()).addTab(position);
				}
			} else {
				((IRCBinder) service).getService().connectToServer(serverName);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
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

		return rootView;
	}

	@Override
	public void onServerWriteNeeded(String message) {
			writeToTextView(message);
	}

	@Override
	public void onNewChannelJoined(String channelName, String nick, String buffer) {
			ChannelFragment channel = new ChannelFragment();
			Bundle b = new Bundle();
			b.putString("channel", channelName);
			b.putString("nick", nick);
			b.putString("serverName", serverName);
			b.putString("buffer", buffer);
			channel.setArguments(b);

			int position = ((ServerChannelActivity) getActivity()).mSectionsPagerAdapter
					.addView(channel);
			((ServerChannelActivity) getActivity()).addTab(position);
	}
}