package com.fusionx.holoirc.misc;

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
}