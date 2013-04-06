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

package com.fusionx.lightirc.misc;

import java.io.IOException;
import java.util.HashMap;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.events.ActionEvent;

import android.os.Parcel;
import android.os.Parcelable;

public class LightPircBotX extends PircBotX implements Parcelable {
	public int noOfAutoJoinChannels;
	public String[] mAutoJoinChannels;
	private HashMap<String, String> mChannelBuffers = new HashMap<String, String>();

	public String mServerBuffer = "";
	public String mServerPassword = "";
	private String mTitle;
	public String mURL = "";
	public String mUserName = "";
	private boolean mIsStarted = false;

	// Getters and setters
	public String getServerBuffer() {
		return mServerBuffer;
	}

	public HashMap<String, String> getChannelBuffers() {
		return mChannelBuffers;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public boolean isStarted() {
		return mIsStarted;
	}

	public void setStarted(boolean started) {
		mIsStarted = started;
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
		setLogin(in.readString());
		mServerPassword = in.readString();
		mTitle = in.readString();
		noOfAutoJoinChannels = in.readInt();
		mAutoJoinChannels = new String[noOfAutoJoinChannels];
		in.readStringArray(mAutoJoinChannels);
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public LightPircBotX createFromParcel(Parcel in) {
			LightPircBotX c = new LightPircBotX();
			c.readFromParcel(in);
			return c;
		}

		public LightPircBotX[] newArray(int size) {
			return new LightPircBotX[size];
		}
	};
}