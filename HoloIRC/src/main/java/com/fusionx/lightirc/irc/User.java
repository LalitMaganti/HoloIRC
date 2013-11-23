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

import com.fusionx.lightirc.util.MiscUtils;

public abstract class User {

    protected String mNick;

    final String mColourCode;

    final UserChannelInterface mUserChannelInterface;

    User(final String nick, final UserChannelInterface userChannelInterface) {
        mNick = nick;
        mUserChannelInterface = userChannelInterface;

        mColourCode = "<color=" + MiscUtils.generateRandomColor(MiscUtils
                .getUserColorOffset(userChannelInterface.getContext())) + ">%1$s</color>";
    }

    public String getColorfulNick() {
        return String.format(mColourCode, mNick);
    }

    @Override
    public String toString() {
        return mNick;
    }

    // Getters and setters
    public String getNick() {
        return mNick;
    }

    public void setNick(String nick) {
        mNick = nick;
    }
}