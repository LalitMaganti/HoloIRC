package com.fusionx.lightirc.irc;

import java.util.ArrayList;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

public class LightChannel extends Channel {
	public LightChannel(PircBotX bot, String name) {
		super(bot, name);
	}

	private String mBuffer;

	public String[] getUserNicks() {
		ArrayList<String> array = new ArrayList<String>();
		for (User user : getOps()) {
			array.add("@" + user.getNick());
		}
		for (User user : getHalfOps()) {
			array.add("half@" + user.getNick());
		}
		for (User user : getVoices()) {
			array.add("+" + user.getNick());
		}
		for (User user : getNormalUsers()) {
			array.add(user.getNick());
		}
		String user[] = array.toArray(new String[0]);
		return user;
	}

	public String getBuffer() {
		return mBuffer;
	}

	public void setBuffer(String buffer) {
		mBuffer = buffer;
	}

	public void appendToBuffer(String newMessage) {
		mBuffer += newMessage;
	}
}
