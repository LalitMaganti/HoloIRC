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

package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.fusionx.lightirc.R;

public class Utils {
    public static int getThemeInt(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return Integer.parseInt(preferences.getString(PreferenceKeys.Theme, String.valueOf(R.style.Light)));
    }

    public static boolean themeIsHoloLight(final Context applicationContext) {
        return getThemeInt(applicationContext) == R.style.Light;
    }

    public static String stripPrefixFromNick(final String nick) {
        return IRCUtils.isUserOwnerOrVoice(nick) ? nick.substring(1) : nick;
    }

    public static boolean isMotdAllowed(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return preferences.getBoolean(PreferenceKeys.Motd, true);
    }

    public static boolean isMessagesFromChannelShown(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return !preferences.getBoolean(PreferenceKeys.HideMessages, false);
    }

    public static String getPartReason(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return preferences.getString(PreferenceKeys.PartReason, "");
    }

    public static String getQuitReason(final Context applicationContext) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return preferences.getString(PreferenceKeys.QuitReason, "");
    }
}