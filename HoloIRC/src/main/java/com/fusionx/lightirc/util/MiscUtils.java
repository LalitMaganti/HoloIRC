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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceConstants;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerStatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

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

    public static Set<String> getIgnoreList(final Context context, final String fileName) {
        if (ignoreList == null) {
            final SharedPreferences preferences = context.getSharedPreferences(fileName,
                    Context.MODE_PRIVATE);
            ignoreList = SharedPreferencesUtils.getStringSet(preferences,
                    PreferenceConstants.PREF_IGNORE_LIST, new HashSet<String>());
        }
        return ignoreList;
    }

    public static void onUpdateIgnoreList(final Server server, final Set<String> set) {
        ignoreList = set;
        server.setIgnoreList(set);
    }

    public static String getAppVersion(final Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final String packageName = context.getPackageName();
            final PackageInfo info = manager.getPackageInfo(packageName, 0);
            return info.versionName + (packageName.endsWith(".debug") ? "-debug" : "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStatusString(final Context context, final ServerStatus serverStatus) {
        switch (serverStatus) {
            case CONNECTED:
                return context.getString(R.string.status_connected);
            case CONNECTING:
                return context.getString(R.string.status_connecting);
            case DISCONNECTED:
                return context.getString(R.string.status_disconnected);
            default:
                return null;
        }
    }
}