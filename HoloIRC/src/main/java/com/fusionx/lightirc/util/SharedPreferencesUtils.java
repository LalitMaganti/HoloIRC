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

import com.fusionx.lightirc.misc.PreferenceConstants;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesUtils {

    public static String getSharedPreferencesPath(final Context context) {
        return context.getFilesDir().getAbsolutePath().replace("files", "shared_prefs/");
    }

    public static void firstTimeServerSetup(final Context context) {
        final AssetManager assetManager = context.getAssets();
        final String[] files;
        try {
            files = assetManager.list("");
            for (String filename : files) {
                if (filename.endsWith(".xml")) {
                    final InputStream in = assetManager.open(filename);
                    final File outFile = new File(getSharedPreferencesPath(context));
                    if (outFile.exists() || outFile.mkdir()) {
                        final File file = new File(getSharedPreferencesPath(context), filename);
                        final FileOutputStream out = new FileOutputStream(file);

                        byte[] buffer = new byte[2048];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }

                        in.close();
                        out.flush();
                        out.close();
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static ServerConfiguration.Builder convertPrefsToBuilder(final Context context,
            final String filename) {
        final SharedPreferences serverSettings = context.getSharedPreferences(filename,
                MODE_PRIVATE);
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

        // Server connection
        builder.setTitle(serverSettings.getString(PreferenceConstants.PREF_TITLE, ""));
        builder.setUrl(serverSettings.getString(PreferenceConstants.PREF_URL, "").trim());
        builder.setPort(Integer.parseInt(serverSettings.getString(PreferenceConstants.PREF_PORT,
                "6667")));

        // SSL
        builder.setSsl(serverSettings.getBoolean(PreferenceConstants.PREF_SSL, false));
        builder.setSslAcceptAllCertificates(serverSettings.getBoolean(PreferenceConstants
                .PREF_SSL_ACCEPT_ALL_CONNECTIONS, false));

        // User settings
        final String firstChoice = serverSettings.getString(PreferenceConstants.PREF_NICK,
                "HoloIRCUser");
        final String secondChoice = serverSettings
                .getString(PreferenceConstants.PREF_SECOND_NICK, "");
        final String thirdChoice = serverSettings
                .getString(PreferenceConstants.PREF_THIRD_NICK, "");
        final NickStorage nickStorage = new NickStorage(firstChoice, secondChoice, thirdChoice);
        builder.setNickStorage(nickStorage);
        builder.setRealName(serverSettings.getString(PreferenceConstants.PREF_REALNAME, "HoloIRC"));
        builder.setNickChangeable(serverSettings.getBoolean(PreferenceConstants.PREF_AUTO_NICK,
                true));

        // Autojoin channels
        final ArrayList<String> auto = new ArrayList<String>(getStringSet(serverSettings,
                PreferenceConstants.PREF_AUTOJOIN, new HashSet<String>()));
        for (final String channel : auto) {
            builder.getAutoJoinChannels().add(channel);
        }

        // Server authorisation
        builder.setServerUserName(serverSettings.getString(PreferenceConstants.PREF_LOGIN_USERNAME,
                "holoirc"));
        builder.setServerPassword(serverSettings.getString(PreferenceConstants.PREF_LOGIN_PASSWORD,
                ""));

        // SASL authorisation
        builder.setSaslUsername(
                serverSettings.getString(PreferenceConstants.PREF_SASL_USERNAME, ""));
        builder.setSaslPassword(
                serverSettings.getString(PreferenceConstants.PREF_SASL_PASSWORD, ""));

        // NickServ authorisation
        builder.setNickservPassword(serverSettings.getString(PreferenceConstants
                .PREF_NICKSERV_PASSWORD, ""));
        return builder;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putStringSet(SharedPreferences preferences, final String key,
            final Set<String> set) {
        final SharedPreferences.Editor editor = preferences.edit();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(key, set);
        } else {
            // removes old occurrences of key
            for (String k : preferences.getAll().keySet()) {
                if (k.startsWith(key)) {
                    editor.remove(k);
                }
            }

            int i = 0;
            for (String value : set) {
                editor.putString(key + i++, value);
            }
        }
        editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(final SharedPreferences pref, final String key,
            final Set<String> defaultValue) {
        if (UIUtils.hasHoneycomb()) {
            return pref.getStringSet(key, defaultValue);
        } else {
            final Set<String> set = new HashSet<String>();

            int i = 0;

            Set<String> keySet = pref.getAll().keySet();
            while (keySet.contains(key + i)) {
                set.add(pref.getString(key + i, ""));
                i++;
            }

            if (set.isEmpty()) {
                return defaultValue;
            } else {
                return set;
            }
        }
    }

    public static List<File> getOldServers(final Context context) {
        final ArrayList<File> array = new ArrayList<>();
        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath(context));

        for (final File file : folder.listFiles()) {
            if (!SharedPreferencesUtils.isExcludedString(file.getName())) {
                array.add(file);
            }
        }
        return array;
    }

    public static void migrateToDatabase(final List<File> array, final Context context) {
        final BuilderDatabaseSource source = new BuilderDatabaseSource(context);
        source.open();
        for (final File file : array) {
            final ServerConfiguration.Builder builder = convertPrefsToBuilder(context,
                    file.getName().replace(".xml", ""));
            source.addServer(builder);
            file.delete();
        }
        source.close();
    }

    public static boolean isExcludedString(final String fileName) {
        return fileName.equals("main.xml") || fileName.contains("com.fusionx.lightirc") ||
                fileName.equals("showcase_internal.xml");
    }

    public static ServerConfiguration.Builder getDefaultNewServer(final Context context) {
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (context);
        final String firstNick = preferences.getString(PreferenceConstants.PREF_DEFAULT_FIRST_NICK,
                "holoirc");
        final String secondNick = preferences
                .getString(PreferenceConstants.PREF_DEFAULT_SECOND_NICK, "");
        final String thirdNick = preferences
                .getString(PreferenceConstants.PREF_DEFAULT_THIRD_NICK, "");
        builder.setNickStorage(new NickStorage(firstNick, secondNick, thirdNick));

        final String realName = preferences.getString(PreferenceConstants.PREF_DEFAULT_REALNAME,
                "HoloIRCUser");
        builder.setRealName(realName);

        final boolean autoNick = preferences.getBoolean(PreferenceConstants
                .PREF_DEFAULT_AUTO_NICK, true);
        builder.setNickChangeable(autoNick);

        return builder;
    }

    public static void onInitialSetup(final Context context) {
        final SharedPreferences globalSettings = context.getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);

        if (firstRun) {
            firstTimeServerSetup(context);
            globalSettings.edit().putBoolean("firstrun", false).commit();
        }
    }
}
