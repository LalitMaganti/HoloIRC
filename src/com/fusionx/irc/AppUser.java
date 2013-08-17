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

import java.util.HashSet;

import lombok.Getter;
import lombok.NonNull;

public class AppUser extends User {
    @Getter
    private HashSet<User> privateMessages = new HashSet<>();

    public AppUser(@NonNull final String nick,
                   @NonNull final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
        userChannelInterface.putAppUser(this);
    }

    public void newPrivateMessage(User user) {
        privateMessages.add(user);
        user.registerHandler();
    }

    public void closePrivateMessage(User user) {
        privateMessages.remove(user);
        user.unregisterHandler();
    }

    public boolean isPrivateMessageOpen(User user) {
        return privateMessages.contains(user);
    }
}
