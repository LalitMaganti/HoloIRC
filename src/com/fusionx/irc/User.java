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

import com.fusionx.Utils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public abstract class User {
    protected String nick;

    @Getter(AccessLevel.NONE)
    protected String nickHTML;

    @Getter(AccessLevel.NONE)
    protected final UserChannelInterface userChannelInterface;

    public User(@NonNull final String nick,
                @NonNull final UserChannelInterface userChannelInterface) {
        this.nick = nick;
        this.userChannelInterface = userChannelInterface;

        nickHTML = "<font color=\"" + Utils.generateRandomColor(Utils
                .getUserColorOffset(userChannelInterface.getContext())) + "\">%1$s</font>";
    }

    public String getColorfulNick() {
        return String.format(nickHTML, nick);
    }
}