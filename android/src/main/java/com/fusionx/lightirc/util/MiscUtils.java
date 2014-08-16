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

import com.fusionx.bus.Bus;
import com.fusionx.lightirc.R;
import co.fusionx.relay.ConnectionStatus;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Full of static utility methods
 *
 * @author Lalit Maganti
 */
public class MiscUtils {

    private static final Bus sBus = new Bus();

    /**
     * Static utility methods only - can't instantiate this class
     */
    private MiscUtils() {
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

    public static String getStatusString(final Context context,
            final ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case CONNECTED:
                return context.getString(R.string.status_connected);
            case RECONNECTING:
                return context.getString(R.string.reconnecting);
            case CONNECTING:
                return context.getString(R.string.status_connecting);
            case DISCONNECTED:
                return context.getString(R.string.status_disconnected);
            default:
                return null;
        }
    }

    public static Bus getBus() {
        return sBus;
    }
}