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

import java.util.HashMap;

import org.pircbotx.PircBotX;

public class LightPircBotX extends PircBotX {	
	public String[] mAutoJoinChannels;
	public HashMap<String, String> mChannelBuffers = new HashMap<String, String>();
	public String mNick = "";
	public String mServerBuffer = "";
	public String mServerPassword = "";
	private String mTitle;
	public String mURL = "";
	public String mUserName = "";
	public boolean mIsStarted = false;
	
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

	public HashMap<String, String> toHashMap() {
		HashMap<String, String> nameIcons = new HashMap<String, String>();
		nameIcons.put("url", mURL);
		nameIcons.put("userName", mUserName);
		nameIcons.put("nick", mNick);
		nameIcons.put("serverPassword", mServerPassword);
		nameIcons.put("title", mTitle);

		return nameIcons;
	}
}