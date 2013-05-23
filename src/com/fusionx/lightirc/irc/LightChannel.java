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

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;

import java.util.ArrayList;

public class LightChannel extends Channel {
    private String mBuffer = "";

    LightChannel(PircBotX bot, UserChannelDao dao, String name) {
        super(bot, dao, name);
    }

// --Commented out by Inspection START (23/05/13 09:59):
//    public ArrayList<String> getUserNicks() {
//        ArrayList<String> array = new ArrayList<String>();
//        for (User user : getOps()) {
//            array.add("@" + user.getNick());
//        }
//        for (User user : getHalfOps()) {
//            array.add("half@" + user.getNick());
//        }
//        for (User user : getVoices()) {
//            array.add("+" + user.getNick());
//        }
//        for (User user : getNormalUsers()) {
//            array.add(user.getNick());
//        }
//        return array;
//    }
// --Commented out by Inspection STOP (23/05/13 09:59)

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