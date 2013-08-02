package com.fusionx.ircinterface.parser;

import com.fusionx.ircinterface.Channel;
import com.fusionx.ircinterface.User;
import com.fusionx.ircinterface.UserChannelInterface;
import com.fusionx.ircinterface.misc.Utils;
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
