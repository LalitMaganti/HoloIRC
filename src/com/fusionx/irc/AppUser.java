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

package com.fusionx.irc;

import java.util.ArrayList;
import java.util.Iterator;

import lombok.NonNull;

public class AppUser extends ChannelUser {
    private ArrayList<PrivateMessageUser> privateMessages = new ArrayList<>();
    private static AppUser user;

    private AppUser(final String nick,
                    final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
        userChannelInterface.putAppUser(this);
    }

    public static AppUser getAppUser(@NonNull final String nick,
                                     @NonNull final UserChannelInterface userChannelInterface) {
        if (user == null) {
            user = new AppUser(nick, userChannelInterface);
        }
        return user;
    }

    public void newPrivateMessage(final PrivateMessageUser user) {
        privateMessages.add(user);
    }

    public void closePrivateMessage(final PrivateMessageUser user) {
        privateMessages.remove(user);
    }

    public boolean isPrivateMessageOpen(final PrivateMessageUser user) {
        return privateMessages.contains(user);
    }

    public Iterator<PrivateMessageUser> getPrivateMessageIterator() {
        return privateMessages.iterator();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof AppUser && ((AppUser) o).getNick().equals(nick);
    }
}
