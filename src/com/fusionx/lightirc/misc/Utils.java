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

package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.fusionx.lightirc.R;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class Utils {
    private static Typeface robotoTypeface = null;

    public static int getThemeInt(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext);
        return Integer.parseInt(preferences.getString(PreferenceKeys.Theme,
                String.valueOf(R.style.Light)));
    }

    public static int getThemeTextColor(final Context applicationContext) {
        return themeIsHoloLight(applicationContext) ?
                applicationContext.getResources().getColor(android.R.color.black) :
                applicationContext.getResources().getColor(android.R.color.white);
    }

    public static boolean themeIsHoloLight(final Context applicationContext) {
        return getThemeInt(applicationContext) == R.style.Light;
    }

    public static boolean isMotdAllowed(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext);
        return preferences.getBoolean(PreferenceKeys.Motd, true);
    }

    public static boolean isMessagesFromChannelShown(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext);
        return !preferences.getBoolean(PreferenceKeys.HideMessages, false);
    }

    public static Typeface getRobotoLightTypeface(final Context context) {
        if (robotoTypeface == null) {
            robotoTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }
        return robotoTypeface;
    }

    public static void setTypeface(final Context context, final TextView textView) {
        final Typeface font = getRobotoLightTypeface(context);
        textView.setTypeface(font);
    }

    public static String getPartReason(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext);
        return preferences.getString(PreferenceKeys.PartReason, "");
    }

    public static ArrayList<String> splitLineBySpaces(final String rawLine) {
        if (rawLine != null) {
            final ArrayList<String> list = new ArrayList<>();
            String buffer = "";

            for (int i = 0; i < rawLine.length(); i++) {
                final char c = rawLine.charAt(i);
                if (c == ' ') {
                    list.add(buffer);
                    buffer = "";
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
}