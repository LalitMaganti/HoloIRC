package com.fusionx.irc.parser;

import com.fusionx.irc.Channel;
import com.fusionx.irc.User;
import com.fusionx.irc.UserChannelInterface;
import com.fusionx.irc.misc.Utils;

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
        final User user = mUserChannelInterface.getUser(parsedArray.get(4));
        user.processWhoMode(parsedArray.get(5), whoChannel);
        if (StringUtils.isEmpty(user.getLogin())) {
            user.setLogin(parsedArray.get(1));
            user.setHost(parsedArray.get(2));
            user.setServerUrl(parsedArray.get(3));
            final ArrayList<String> secondParse = Utils.splitRawLine(parsedArray.get(6)
                    .substring(2));
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
