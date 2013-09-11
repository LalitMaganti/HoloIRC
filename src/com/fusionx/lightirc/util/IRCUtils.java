package com.fusionx.lightirc.util;

import org.apache.commons.lang3.StringUtils;

public class IRCUtils {
    public static boolean areNicksEqual(final String firstNick, final String secondNick) {
        return firstNick.equals(secondNick) || (firstNick.equalsIgnoreCase(secondNick) &&
                (firstNick.equalsIgnoreCase("nickserv") || firstNick.equalsIgnoreCase
                        ("chanserv")));
    }

    public static String getNickFromRaw(final String rawSource) {
        String nick;
        if (rawSource.contains("!") && rawSource.contains("@")) {
            nick = StringUtils.substringBefore(rawSource, "!");
        } else {
            nick = rawSource;
        }
        return nick;
    }

    public static String getHostNameFromRaw(final String rawSource) {
        String nick;
        if (rawSource.contains("!") && rawSource.contains("@")) {
            nick = StringUtils.substringAfter(rawSource, "@");
        } else {
            nick = rawSource;
        }
        return nick;
    }

    public static String getNickFromNameReply(final String rawNameNick) {
        String nickToReturn;
        final char firstChar = rawNameNick.charAt(0);
        // TODO - fix this up
        if (firstChar == '~' || firstChar == '&' || firstChar == '@' || firstChar == '%' ||
                firstChar == '+') {
            nickToReturn = rawNameNick.substring(1);
        } else {
            nickToReturn = rawNameNick;
        }
        return nickToReturn;
    }
}
