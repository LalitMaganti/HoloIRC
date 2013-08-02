package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import com.fusionx.ircinterface.ServerConfiguration;

public class FileConfigurationConverter {
    public static ServerConfiguration.Builder convertFileToBuilder(final Context context,
                                                                   final String filename) {
        final SharedPreferences serverSettings = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();
        builder.setTitle(serverSettings.getString(PreferenceKeys.Title, ""));
        builder.setUrl(serverSettings.getString(PreferenceKeys.URL, ""));
        builder.setPort(Integer.parseInt(serverSettings.getString(PreferenceKeys.Port, "6667")));
        builder.setSsl(serverSettings.getBoolean(PreferenceKeys.SSL, false));

        builder.setNick(serverSettings.getString(PreferenceKeys.Nick, ""));
        builder.setRealName(serverSettings.getString(PreferenceKeys.RealName, "HoloIRC"));
        //builder.setAutoNickChange(serverSettings.getBoolean(PreferenceKeys.AutoNickChange, true));

        //final Set<String> auto = serverSettings.getStringSet(PreferenceKeys.AutoJoin, new HashSet<String>());
        //for (final String channel : auto) {
        //builder.addAutoJoinChannel(channel);
        //}

        builder.setServerUserName(serverSettings.getString(PreferenceKeys.ServerUserName, "holoirc"));
        builder.setServerPassword(serverSettings.getString(PreferenceKeys.ServerPassword, ""));

        final String nickServPassword = serverSettings.getString(PreferenceKeys.NickServPassword, null);
        if (nickServPassword != null && !nickServPassword.equals("")) {
            builder.setNickservPassword(nickServPassword);
        }

        builder.setFile(filename);
        return builder;
    }
}
