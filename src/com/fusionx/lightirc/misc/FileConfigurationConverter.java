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

import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.util.MiscUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class FileConfigurationConverter {
    public static ServerConfiguration.Builder convertFileToBuilder(final Context context,
                                                                   final String filename) {
        final SharedPreferences serverSettings = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

        // Server connection
        builder.setTitle(serverSettings.getString(PreferenceConstants.Title, ""));
        builder.setUrl(serverSettings.getString(PreferenceConstants.URL, "").trim());
        builder.setPort(Integer.parseInt(serverSettings.getString(PreferenceConstants.Port, "6667")));
        builder.setSsl(serverSettings.getBoolean(PreferenceConstants.SSL, false));

        // User settings
        final String firstChoice = serverSettings.getString(PreferenceConstants.FirstNick,
                "HoloIRCUser");
        final String secondChoice = serverSettings.getString(PreferenceConstants.SecondNick, "");
        final String thirdChoice = serverSettings.getString(PreferenceConstants.ThirdNick, "");
        final ServerConfiguration.NickStorage nickStorage = new ServerConfiguration.NickStorage
                (firstChoice, secondChoice, thirdChoice);
        builder.setNickStorage(nickStorage);
        builder.setRealName(serverSettings.getString(PreferenceConstants.RealName, "HoloIRC"));
        builder.setNickChangeable(serverSettings.getBoolean(PreferenceConstants.AutoNickChange, true));

        // Autojoin channels
        final ArrayList<String> auto = new ArrayList<>(MiscUtils.getStringSet(serverSettings,
                PreferenceConstants.AutoJoin, new HashSet<String>()));
        for (final String channel : auto) {
            builder.getAutoJoinChannels().add(channel);
        }

        // Server authorisation
        builder.setServerUserName(serverSettings.getString(PreferenceConstants.ServerUserName, "holoirc"));
        builder.setServerPassword(serverSettings.getString(PreferenceConstants.ServerPassword, ""));

        // SASL authorisation
        builder.setSaslUsername(serverSettings.getString(PreferenceConstants.SaslUsername, ""));
        builder.setSaslPassword(serverSettings.getString(PreferenceConstants.SaslPassword, ""));

        // NickServ authorisation
        builder.setNickservPassword(serverSettings.getString(PreferenceConstants.NickServPassword, ""));

        builder.setFile(filename);
        return builder;
    }
}