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

import com.fusionx.lightirc.utils.Utils;
import com.fusionx.lightirc.irc.core.ServerConfiguration;

import java.util.ArrayList;
import java.util.HashSet;

public class FileConfigurationConverter {
    public static ServerConfiguration.Builder convertFileToBuilder(final Context context,
                                                                   final String filename) {
        final SharedPreferences serverSettings = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

        // Server connection
        builder.setTitle(serverSettings.getString(PreferenceKeys.Title, ""));
        builder.setUrl(serverSettings.getString(PreferenceKeys.URL, "").trim());
        builder.setPort(Integer.parseInt(serverSettings.getString(PreferenceKeys.Port, "6667")));
        builder.setSsl(serverSettings.getBoolean(PreferenceKeys.SSL, false));

        // User settings
        final String firstChoice = serverSettings.getString(PreferenceKeys.FirstNick,
                "HoloIRCUser");
        final String secondChoice = serverSettings.getString(PreferenceKeys.SecondNick, "");
        final String thirdChoice = serverSettings.getString(PreferenceKeys.ThirdNick, "");
        final ServerConfiguration.NickStorage nickStorage = new ServerConfiguration.NickStorage
                (firstChoice, secondChoice, thirdChoice);
        builder.setNickStorage(nickStorage);
        builder.setRealName(serverSettings.getString(PreferenceKeys.RealName, "HoloIRC"));
        builder.setNickChangeable(serverSettings.getBoolean(PreferenceKeys.AutoNickChange, true));

        // Autojoin channels
        final ArrayList<String> auto = new ArrayList<>(Utils.getStringSet(serverSettings,
                PreferenceKeys.AutoJoin, new HashSet<String>()));
        for (final String channel : auto) {
            builder.getAutoJoinChannels().add(channel);
        }

        // Server authorisation
        builder.setServerUserName(serverSettings.getString(PreferenceKeys.ServerUserName, "holoirc"));
        builder.setServerPassword(serverSettings.getString(PreferenceKeys.ServerPassword, ""));

        // SASL authorisation
        builder.setSaslUsername(serverSettings.getString(PreferenceKeys.SaslUsername, ""));
        builder.setSaslPassword(serverSettings.getString(PreferenceKeys.SaslPassword, ""));

        // NickServ authorisation
        builder.setNickservPassword(serverSettings.getString(PreferenceKeys.NickServPassword, ""));

        builder.setFile(filename);
        return builder;
    }
}