package com.fusionx.lightirc.misc;


import android.text.Html;

import java.util.Comparator;

public class UserComparator implements Comparator<String> {
    @Override
    public int compare(final String s, final String s2) {
        final String firstStripped = Html.fromHtml(s).toString();
        final String secondStripped = Html.fromHtml(s2).toString();

        if (firstStripped.startsWith(secondStripped.substring(0, 1)) &&
                (firstStripped.substring(0, 1).equals("@")
                        || firstStripped.substring(0, 1).equals("+"))) {
            final String firstRemoved = firstStripped.substring(1);
            final String secondRemoved = secondStripped.substring(1);
            return firstRemoved.compareToIgnoreCase(secondRemoved);
        } else if (firstStripped.startsWith("@")) {
            return -1;
        } else if (secondStripped.startsWith("@")) {
            return 1;
        } else if (firstStripped.startsWith("+")) {
            return -1;
        } else if (secondStripped.startsWith("+")) {
            return 1;
        }

        return firstStripped.compareToIgnoreCase(secondStripped);
    }
}