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

import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.fusionx.lightirc.constants.PreferenceConstants.Title;

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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> getServersFromPreferences(final Context context) {
        final ArrayList<String> array = new ArrayList<String>();
        final File folder = new File(getSharedPreferencesPath(context));
        for (final String fileName : folder.list()) {
            if (fileName.startsWith("server_")) {
                array.add(migrateFileToNewSystem(context, fileName));
            } else if (!isExcludedString(fileName)) {
                array.add(fileName.replace(".xml", ""));
            }
        }
        Collections.sort(array);
        return array;
    }

    public static String migrateFileToNewSystem(final Context context, final String fileName) {
        final File file = new File(getSharedPreferencesPath(context), fileName);
        final SharedPreferences sharedPreferences = context.getSharedPreferences(StringUtils
                .remove(fileName, ".xml"), Context.MODE_PRIVATE);
        final String newName = sharedPreferences.getString(Title, "").toLowerCase();
        file.renameTo(new File(getSharedPreferencesPath(context),
                newName + ".xml"));
        return newName;
    }

    public static ServerConfiguration.Builder convertPrefsToBuilder(final Context context,
            final String filename) {
        final SharedPreferences serverSettings = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        final ServerConfiguration.Builder builder = new ServerConfiguration.Builder();

        // Server connection
        builder.setTitle(serverSettings.getString(PreferenceConstants.Title, ""));
        builder.setUrl(serverSettings.getString(PreferenceConstants.URL, "").trim());
        builder.setPort(Integer.parseInt(serverSettings.getString(PreferenceConstants.Port,
                "6667")));

        // SSL
        builder.setSsl(serverSettings.getBoolean(PreferenceConstants.SSL, false));
        builder.setSslAcceptAllCertificates(serverSettings.getBoolean(PreferenceConstants
                .SSLAcceptAll, false));

        // User settings
        final String firstChoice = serverSettings.getString(PreferenceConstants.FirstNick,
                "HoloIRCUser");
        final String secondChoice = serverSettings.getString(PreferenceConstants.SecondNick, "");
        final String thirdChoice = serverSettings.getString(PreferenceConstants.ThirdNick, "");
        final NickStorage nickStorage = new NickStorage(firstChoice, secondChoice, thirdChoice);
        builder.setNickStorage(nickStorage);
        builder.setRealName(serverSettings.getString(PreferenceConstants.RealName, "HoloIRC"));
        builder.setNickChangeable(serverSettings.getBoolean(PreferenceConstants.AutoNickChange,
                true));

        // Autojoin channels
        final ArrayList<String> auto = new ArrayList<String>(getStringSet(serverSettings,
                PreferenceConstants.AutoJoin, new HashSet<String>()));
        for (final String channel : auto) {
            builder.getAutoJoinChannels().add(channel);
        }

        // Server authorisation
        builder.setServerUserName(serverSettings.getString(PreferenceConstants.ServerUserName,
                "holoirc"));
        builder.setServerPassword(serverSettings.getString(PreferenceConstants.ServerPassword,
                ""));

        // SASL authorisation
        builder.setSaslUsername(serverSettings.getString(PreferenceConstants.SaslUsername, ""));
        builder.setSaslPassword(serverSettings.getString(PreferenceConstants.SaslPassword, ""));

        // NickServ authorisation
        builder.setNickservPassword(serverSettings.getString(PreferenceConstants
                .NickServPassword, ""));
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

    private static boolean isExcludedString(final String fileName) {
        return fileName.equals("main.xml") || fileName.contains("com.fusionx.lightirc") ||
                fileName.equals("showcase_internal.xml");
    }
}
