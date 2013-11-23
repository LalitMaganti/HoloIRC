/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.irc.parser;

import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.UserChannelInterface;
import com.fusionx.lightirc.irc.event.Event;

import java.util.ArrayList;

class WhoParser {

    private final UserChannelInterface mUserChannelInterface;

    private Channel mWhoChannel;

    private final String mServerTitle;

    WhoParser(UserChannelInterface userChannelInterface, final String serverTitle) {
        mUserChannelInterface = userChannelInterface;
        mServerTitle = serverTitle;
    }

    Event parseWhoReply(final ArrayList<String> parsedArray) {
        if (mWhoChannel == null) {
            mWhoChannel = mUserChannelInterface.getChannel(parsedArray.get(0));
        }
        final ChannelUser user = mUserChannelInterface.getUser(parsedArray.get(4));
        user.processWhoMode(parsedArray.get(5), mWhoChannel);
        return new Event(user.getNick());
    }

    Event parseWhoFinished() {
        if (mWhoChannel != null && mWhoChannel.getUsers() != null) {
            final MessageSender sender = MessageSender.getSender(mServerTitle);
            final Event event = sender.sendGenericChannelEvent(mWhoChannel, "", true);
            mWhoChannel = null;
            return event;
        } else {
            return new Event("null");
        }
    }
}
