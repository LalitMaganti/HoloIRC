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

package com.fusionx.irc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.fusionx.irc.constants.Constants;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.lightirc.misc.PreferenceKeys;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.NonNull;

import static com.fusionx.lightirc.misc.Utils.themeIsHoloLight;

public class Utils {
    /**
     * Split the line received from the server into it's components
     *
     * @param rawLine the line received from the server
     * @return the parsed list
     */
    public static ArrayList<String> splitRawLine(final String rawLine) {
        if (rawLine != null) {
            final ArrayList<String> list = new ArrayList<>();
            String buffer = "";
            final String colonLessLine = rawLine.charAt(0) == ':' ? rawLine.substring(1) : rawLine;

            for (int i = 0; i < colonLessLine.length(); i++) {
                final char c = colonLessLine.charAt(i);
                if (c == ' ') {
                    list.add(buffer);
                    buffer = "";
                } else if (c == ':' && StringUtils.isEmpty(buffer)) {
                    // A colon can occur in an IPv6 address so that is why the buffer check
                    // is necessary - the final colon can only occur with an empty buffer
                    // Add all the stuff after the last colon as a single item
                    // Essentially the first colon that occurs when the buffer is empty
                    list.add(colonLessLine.substring(i + 1));
                    break;
                } else {
                    buffer += c;
                }
            }

            if (!StringUtils.isEmpty(buffer)) {
                list.add(buffer);
            }
            return list;
        } else {
            return null;
        }
    }

    public static String convertArrayListToString(final ArrayList<String> list) {
        final StringBuilder builder = new StringBuilder();
        for (final String item : list) {
            builder.append(item).append(" ");
        }
        return builder.toString().trim();
    }

    public static void removeFirstElementFromList(final List<String> list, final int noOfTimes) {
        for (int i = 1; i <= noOfTimes; i++) {
            list.remove(0);
        }
    }

    public static Bundle parcelDataForBroadcast(final String destination,
                                                @NonNull final Enum type,
                                                @NonNull final String... message) {
        final Bundle event = new Bundle();
        if (destination != null) {
            event.putString(EventBundleKeys.destination, destination);
        }
        event.putSerializable(EventBundleKeys.eventType, type);
        event.putString(EventBundleKeys.message, message[0]);

        return event;
    }

    public static String getNickFromRaw(final String rawSource) {
        String nick;
        if (rawSource.contains("!") && rawSource.contains("@")) {
            final int indexOfExclamation = rawSource.indexOf('!');
            nick = StringUtils.left(rawSource, indexOfExclamation);
        } else {
            nick = rawSource;
        }
        return nick;
    }

    public static int generateRandomColor(final int colorOffset) {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red + colorOffset) / 2;
        green = (green + colorOffset) / 2;
        blue = (blue + colorOffset) / 2;

        return Color.rgb(red, green, blue);
    }

    public static int getUserColorOffset(final Context context) {
        return themeIsHoloLight(context) ? 0 : 255;
    }

    public static boolean isChannel(String rawName) {
        return Constants.channelPrefixes.contains(rawName.charAt(0));
    }

    public static String getQuitReason(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext);
        return preferences.getString(PreferenceKeys.QuitReason, "");
    }
}
