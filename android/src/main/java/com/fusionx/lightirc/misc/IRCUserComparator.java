package com.fusionx.lightirc.misc;

import java.util.Comparator;

import co.fusionx.relay.constant.UserLevel;
import co.fusionx.relay.conversation.Channel;
import co.fusionx.relay.core.ChannelUser;

public class IRCUserComparator implements Comparator<ChannelUser> {

    private final Channel mChannel;

    public IRCUserComparator(final Channel channel) {
        mChannel = channel;
    }

    @Override
    public int compare(final ChannelUser lhs, final ChannelUser rhs) {
        final UserLevel firstUserMode = lhs.getChannelPrivileges(mChannel);
        final UserLevel secondUserMode = rhs.getChannelPrivileges(mChannel);

        /**
         * Code for compatibility with objects being removed
         */
        if (firstUserMode == null && secondUserMode == null) {
            return 0;
        } else if (firstUserMode == null) {
            return -1;
        } else if (secondUserMode == null) {
            return 1;
        }

        if (firstUserMode.equals(secondUserMode)) {
            final String firstRemoved = lhs.getNick().getNickAsString();
            final String secondRemoved = rhs.getNick().getNickAsString();

            return firstRemoved.compareToIgnoreCase(secondRemoved);
        } else if (firstUserMode.ordinal() > secondUserMode.ordinal()) {
            return 1;
        } else {
            return -1;
        }
    }
}