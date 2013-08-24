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

package com.fusionx.irc.parser;

import com.fusionx.common.Utils;
import com.fusionx.irc.Channel;
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.UserChannelInterface;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class WhoParser {
    private final UserChannelInterface mUserChannelInterface;
    private Channel whoChannel;

    WhoParser(UserChannelInterface userChannelInterface) {
        this.mUserChannelInterface = userChannelInterface;
    }

    void parseWhoReply(final ArrayList<String> parsedArray) {
        if (whoChannel == null) {
            whoChannel = mUserChannelInterface.getChannel(parsedArray.get(0));
        }
        final ChannelUser user = mUserChannelInterface.getUser(parsedArray.get(4));
        user.processWhoMode(parsedArray.get(5), whoChannel);
        if (StringUtils.isEmpty(user.getLogin())) {
            user.setLogin(parsedArray.get(1));
            user.setHost(parsedArray.get(2));
            user.setServerUrl(parsedArray.get(3));
            final ArrayList<String> secondParse = Utils.splitRawLine(parsedArray.get(6)
                    .substring(2), true);
            user.setRealName(Utils.convertArrayListToString(secondParse));
        }
        mUserChannelInterface.addChannelToUser(user, whoChannel);
        whoChannel.getUsers().markForAddition(user);
    }

    void parseWhoFinished() {
        whoChannel.getUsers().addMarked();
        whoChannel = null;
    }
}
