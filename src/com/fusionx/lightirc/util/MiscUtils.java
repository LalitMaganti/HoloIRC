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

package com.fusionx.lightirc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.fusionx.lightirc.constants.Constants;
import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.constants.UserLevelEnum;
import com.google.common.base.CharMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Full of static utility methods
 *
 * @author Lalit Maganti
 */
public class MiscUtils {
    public static boolean isMotdAllowed(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getBoolean(PreferenceConstants.Motd, true);
    }

    public static boolean isMessagesFromChannelShown(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return !preferences.getBoolean(PreferenceConstants.HideMessages, false);
    }

    public static String getPartReason(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(PreferenceConstants.PartReason, "");
    }

    /**
     * Split the line received from the server into it's components
     *
     * @param input          the line received from the server
     * @param careAboutColon - whether a colon means the rest of the line should be added in one go
     * @return the parsed list
     */
    public static ArrayList<String> splitRawLine(final String input,
                                                 final boolean careAboutColon) {
        ArrayList<String> stringParts = new ArrayList<>();
        if (input == null || input.length() == 0)
            return stringParts;

        final String colonLessLine = input.charAt(0) == ':' ? input.substring(1) : input;
        //Heavily optimized version string split by space with all characters after :
        //added as a single entry. Under benchmarks, its faster than StringTokenizer,
        //String.split, toCharArray, and charAt
        String trimmedInput = CharMatcher.WHITESPACE.trimFrom(colonLessLine);
        int pos = 0, end;
        while ((end = trimmedInput.indexOf(' ', pos)) >= 0) {
            stringParts.add(trimmedInput.substring(pos, end));
            pos = end + 1;
            if (trimmedInput.charAt(pos) == ':' && careAboutColon) {
                stringParts.add(trimmedInput.substring(pos + 1));
                return stringParts;
            }
        }
        //No more spaces, add last part of line
        stringParts.add(trimmedInput.substring(pos));
        return stringParts;
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

    public static int generateRandomColor(final int colorOffset) {
        final Random random = new Random();
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
        return UIUtils.isThemeLight(context) ? 0 : 255;
    }

    public static boolean isChannel(String rawName) {
        return Constants.channelPrefixes.contains(rawName.charAt(0));
    }

    public static String getQuitReason(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(PreferenceConstants.QuitReason, "");
    }

    public static int getNumberOfReconnectEvents(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getInt(PreferenceConstants.ReconnectTries, 3);
    }

    public static Set<String> getIgnoreList(final Context context, final String fileName) {
        final SharedPreferences preferences = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return SharedPreferencesUtils.getStringSet(preferences, PreferenceConstants.IgnoreList,
                new HashSet<String>());
    }

    public static String getAppVersion(final Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isUserOwnerOrVoice(final UserLevelEnum level) {
        return level.equals(UserLevelEnum.OP) || level.equals(UserLevelEnum.VOICE);
    }

    /**
     * Static utility methods only - can't instantiate this class
     */
    private MiscUtils() {
    }
}