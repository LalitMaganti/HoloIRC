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
import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.services.IRCService;
import com.fusionx.lightirc.services.IRCService.IRCBinder;

public class ChannelFragment extends IRCFragment implements OnKeyListener,
		ChannelCallbacks {
	private String serverName;
	public String[] mUserList;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_irc_channel,
				container, false);

		setTitle(getArguments().getString("channel"));
		serverName = getArguments().getString("serverName");
		String buffer = getArguments().getString("buffer");

		writeToTextView(buffer, rootView);

		EditText textview = (EditText) rootView.findViewById(R.id.editText1);
		textview.setOnKeyListener(this);

		Intent service = new Intent(getActivity(), IRCService.class);
		getActivity().bindService(service, mConnection, 0);

		return rootView;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			((IRCBinder) service).getService().setChannelCallbacks(
					ChannelFragment.this, getTitle(), serverName);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	private final ServiceConnection mPartConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder binder) {
			IRCService service = ((IRCBinder) binder).getService();
			service.partFromChannel(serverName, getTitle());
			getActivity().unbindService(this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			// This should never happen
		}
	};

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		EditText editText = (EditText) view;

		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == KeyEvent.KEYCODE_ENTER)
				&& !editText.getText().toString().equals("\n")
				&& !editText.getText().toString().isEmpty()) {
			Intent intent = new Intent();
			intent.setAction("com.fusionx.lightirc.CHANNEL_MESSAGE_TO_PARSE");
			intent.putExtra("channel", getTitle());
			intent.putExtra("serverName", serverName);
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

	@Override
	public void onDestroy() {
		getActivity().unbindService(mConnection);
		super.onDestroy();
	}

	public void part() {
		Intent service = new Intent(getActivity(), IRCService.class);
		getActivity().bindService(service, mPartConnection, 0);
	}

	@Override
	public void userListChanged(String newList[]) {
		mUserList = newList;
		((ServerChannelActivity) getActivity()).userListChanged(newList, this);
	}
}
