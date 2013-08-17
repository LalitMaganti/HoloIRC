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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static com.fusionx.lightirc.misc.PreferenceKeys.AutoNickChange;
import static com.fusionx.lightirc.misc.PreferenceKeys.Nick;
import static com.fusionx.lightirc.misc.PreferenceKeys.Port;
import static com.fusionx.lightirc.misc.PreferenceKeys.SSL;
import static com.fusionx.lightirc.misc.PreferenceKeys.ServerUserName;
import static com.fusionx.lightirc.misc.PreferenceKeys.Title;
import static com.fusionx.lightirc.misc.PreferenceKeys.URL;

public class SharedPreferencesUtils {
    public static String getSharedPreferencesPath(final Context applicationContext) {
        return applicationContext.getFilesDir().getAbsolutePath().replace("files", "shared_prefs/");
    }

    public static void firstTimeServerSetup(final Context context) {
        final HashSet<String> auto = new HashSet<>();

        final SharedPreferences settings = context.getSharedPreferences("server_0",
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor e = settings.edit();
        e.putString(Title, "Freenode").putString(URL, "irc.freenode.net")
                .putString(Port, "6667").putBoolean(SSL, false).putString(Nick, "HoloIRCUser")
                .putBoolean(AutoNickChange, true).putString(ServerUserName, "holoirc")
                .putStringSet(PreferenceKeys.AutoJoin, auto);
        e.commit();
    }

    public static ArrayList<String> getServersFromPreferences(final Context applicationContext) {
        final ArrayList<String> array = new ArrayList<>();
        final File folder = new File(getSharedPreferencesPath(applicationContext));
        for (final String file : folder.list()) {
            if (file.startsWith("server_")) {
                array.add(file.replace(".xml", ""));
            }
        }
        Collections.sort(array);
        return array;
    }
}
