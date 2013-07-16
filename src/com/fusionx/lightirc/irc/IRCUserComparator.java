/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.irc;

import android.text.Html;
import com.fusionx.lightirc.misc.IRCUtils;

import java.util.Comparator;

public class IRCUserComparator implements Comparator<String> {
    @Override
    public int compare(final String s, final String s2) {
        final String firstStripped = Html.fromHtml(s).toString();
        final String secondStripped = Html.fromHtml(s2).toString();

        if (firstStripped.startsWith(secondStripped.substring(0, 1)) && IRCUtils.isUserOwnerOrVoice(firstStripped)) {
            final String firstRemoved = firstStripped.substring(1);
            final String secondRemoved = secondStripped.substring(1);

            return firstRemoved.compareToIgnoreCase(secondRemoved);
        } else if (IRCUtils.isUserOwner(firstStripped)) {
            return -1;
        } else if (IRCUtils.isUserOwner(secondStripped)) {
            return 1;
        } else if (IRCUtils.isUserVoice(firstStripped)) {
            return -1;
        } else if (IRCUtils.isUserVoice(secondStripped)) {
            return 1;
        }

        return firstStripped.compareToIgnoreCase(secondStripped);
    }
}