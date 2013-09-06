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

import android.text.style.ForegroundColorSpan;

import com.fusionx.lightirc.util.MiscUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class User {
    @Getter
    @Setter
    protected String nick;

    protected final String nickHTML;
    protected final ForegroundColorSpan span;
    protected final UserChannelInterface userChannelInterface;

    public User(@NonNull final String nick,
                @NonNull final UserChannelInterface userChannelInterface) {
        this.nick = nick;
        this.userChannelInterface = userChannelInterface;
        span = new ForegroundColorSpan(MiscUtils.generateRandomColor(MiscUtils
                .getUserColorOffset(userChannelInterface.getContext())));

        nickHTML = "<font color=\"" + MiscUtils.generateRandomColor(MiscUtils
                .getUserColorOffset(userChannelInterface.getContext())) + "\">%1$s</font>";
    }

    public String getColorfulNick() {
        return String.format(nickHTML, nick);
    }
}