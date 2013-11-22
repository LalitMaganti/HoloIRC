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

package com.fusionx.lightirc.irc;

import com.fusionx.lightirc.collections.UpdateableTreeSet;

import java.util.ArrayList;
import java.util.Iterator;

public class AppUser extends ChannelUser {

    private ArrayList<PrivateMessageUser> mPrivateMessages = new ArrayList<PrivateMessageUser>();

    public AppUser(final String nick,
            final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
        userChannelInterface.putAppUser(this);
    }

    public void createPrivateMessage(final PrivateMessageUser user) {
        mPrivateMessages.add(user);
    }

    public void closePrivateMessage(final PrivateMessageUser user) {
        mPrivateMessages.remove(user);
    }

    public boolean isPrivateMessageOpen(final PrivateMessageUser user) {
        return mPrivateMessages.contains(user);
    }

    public Iterator<PrivateMessageUser> getPrivateMessageIterator() {
        return mPrivateMessages.iterator();
    }

    @Override
    public UpdateableTreeSet<Channel> getChannels() {
        return userChannelInterface.getAllChannelsInUser(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof AppUser) {
            AppUser us = ((AppUser) o);
            return us.getNick().equals(nick) && us.mServer.equals(mServer);
        } else {
            return false;
        }
    }
}