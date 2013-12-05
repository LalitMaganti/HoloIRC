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

import com.google.common.base.CharMatcher;

import com.fusionx.lightirc.constants.PreferenceConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Full of static utility methods
 *
 * @author Lalit Maganti
 */
public class MiscUtils {

    private static Set<String> ignoreList = null;

    /**
     * Static utility methods only - can't instantiate this class
     */
    private MiscUtils() {
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
        final ArrayList<String> stringParts = new ArrayList<String>();
        if (input == null || input.length() == 0) {
            return stringParts;
        }

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

    public static Set<String> getIgnoreList(final Context context, final String fileName) {
        if (ignoreList == null) {
            final SharedPreferences preferences = context.getSharedPreferences(fileName,
                    Context.MODE_PRIVATE);
            ignoreList = SharedPreferencesUtils.getStringSet(preferences,
                    PreferenceConstants.IgnoreList, new HashSet<String>());
        }
        return ignoreList;
    }

    public static void forceUpdateIgnoreList(final Set<String> set) {
        ignoreList = set;
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
}