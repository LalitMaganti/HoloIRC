package com.fusionx.lightirc.irc;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;

import java.util.ArrayList;

public class LightChannel extends Channel {
    private String mBuffer = "";

    protected LightChannel(PircBotX bot, UserChannelDao dao, String name) {
        super(bot, dao, name);
    }

    public ArrayList<String> getUserNicks() {
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
        return array;
    }

    public ArrayList<String> getCleanUserNicks() {
        ArrayList<String> array = new ArrayList<String>();
        for (User user : getOps()) {
            array.add(user.getNick());
        }
        for (User user : getHalfOps()) {
            array.add(user.getNick());
        }
        for (User user : getVoices()) {
            array.add(user.getNick());
        }
        for (User user : getNormalUsers()) {
            array.add(user.getNick());
        }
        return array;
    }

    public void appendToBuffer(String message) {
        mBuffer += message;
    }

    public String getBuffer() {
        return mBuffer;
    }
}