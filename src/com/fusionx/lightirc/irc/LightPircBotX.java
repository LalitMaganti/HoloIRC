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

package com.fusionx.lightirc.irc;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.events.ActionEvent;

import android.os.Parcel;
import android.os.Parcelable;

public class LightPircBotX extends PircBotX implements Parcelable {
	public int noOfAutoJoinChannels;
	public String[] mAutoJoinChannels;

	private String mBuffer = "";
	public String mServerPassword = "";
	private String mTitle;
	public String mURL = "";
	public String mUserName = "";
	private boolean mStarted = false;

	// Getters and setters
	public String getBuffer() {
		return mBuffer;
	}
	
	public void appendToBuffer(String message) {
		mBuffer += message;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public boolean isStarted() {
		return mStarted;
	}
	
	public Set<String> getChannelNames() {
		HashSet<String> names = new HashSet<String>();
		for(Channel channel : getChannels()) {
			names.add(channel.getName());
		}
		return names;
	}

	// Overriden methods
	@Override
	public Channel getChannel(String name) {
		if (name == null)
			throw new NullPointerException("Can't get a null channel");
		for (Channel curChan : userChanInfo.getAValues())
			if (curChan.getName().equals(name))
				return curChan;

		//Channel does not exist, create one
		LightChannel chan = new LightChannel(this, name);
		userChanInfo.putB(chan);
		return chan;
	}

	// Helper/extension methods
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void sendAction(String target, String action) {
		super.sendAction(target, action);
		getListenerManager()
				.dispatchEvent(
						new ActionEvent(this, getUserBot(), getChannel(target),
								action));
	}

	public void partChannel(String channel) {
		partChannel(getChannel(channel));
	}

	public void connectAndAutoJoin() {
		try {
			mStarted = true;
			connect(mURL);
			for (String channel : mAutoJoinChannels) {
				joinChannel(channel);
			}
		} catch (NickAlreadyInUseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IrcException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> toHashMap() {
		HashMap<String, String> nameIcons = new HashMap<String, String>();
		nameIcons.put("url", mURL);
		nameIcons.put("userName", mUserName);
		nameIcons.put("nick", getLogin());
		nameIcons.put("serverPassword", mServerPassword);
		nameIcons.put("title", mTitle);

		return nameIcons;
	}

	// Parcelable stuff
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(mURL);
		dest.writeString(mUserName);
		dest.writeString(getLogin());
		dest.writeString(mServerPassword);
		dest.writeString(mTitle);
		dest.writeInt(noOfAutoJoinChannels);
		dest.writeStringArray(mAutoJoinChannels);
	}

	private void readFromParcel(Parcel in) {
		mURL = in.readString();
		mUserName = in.readString();

		// TODO - this isn't correct - fix this
		setLogin(in.readString());
		setName(getLogin());

		mServerPassword = in.readString();
		mTitle = in.readString();
		noOfAutoJoinChannels = in.readInt();
		mAutoJoinChannels = new String[noOfAutoJoinChannels];
		in.readStringArray(mAutoJoinChannels);
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		@Override
		public LightPircBotX createFromParcel(Parcel in) {
			LightPircBotX c = new LightPircBotX();
			c.readFromParcel(in);
			return c;
		}

		@Override
		public LightPircBotX[] newArray(int size) {
			return new LightPircBotX[size];
		}
	};
}
