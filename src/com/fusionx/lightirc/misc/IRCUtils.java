package com.fusionx.lightirc.misc;

import com.fusionx.ircinterface.enums.UserLevel;

public class IRCUtils {
    public static boolean isUserVoice(final String user) {
        return user.startsWith("+");
    }

    public static boolean isUserOwner(final String user) {
        return user.startsWith("@");
    }

    public static boolean isUserOwnerOrVoice(final String user) {
        return isUserOwner(user) || isUserVoice(user);
    }

    public static boolean isUserOwnerOrVoice(final UserLevel level) {
        return level.equals(UserLevel.OP) || level.equals(UserLevel.VOICE);
    }
}