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

package app.holoirc.util;

import app.holoirc.misc.PreferenceConstants;
import app.holoirc.model.db.ServerDatabase;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.fusionx.relay.base.ServerConfiguration;
import co.fusionx.relay.misc.NickStorage;

import static android.content.Context.MODE_PRIVATE;
import static app.holoirc.misc.PreferenceConstants.PREF_IGNORE_LIST;

public class SharedPreferencesUtils {

    public static String getSharedPreferencesPath(final Context context) {
        return context.getFilesDir().getAbsolutePath().replace("files", "shared_prefs/");
    }

    // TODO - make these static somewhere
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

        builder.setServerUserName("holoirc");

        return builder;
    }

    public static boolean isInitialDatabaseRun(final Context context) {
        final SharedPreferences globalSettings = context.getSharedPreferences("main", MODE_PRIVATE);
        return globalSettings.getBoolean("firstDbRun", true);
    }

    public static void onInitialSetup(final Context context) {
        final SharedPreferences globalSettings = context.getSharedPreferences("main", MODE_PRIVATE);
        final boolean firstRun = globalSettings.getBoolean("firstrun", true);
        final boolean firstDbRun = globalSettings.getBoolean("firstDbRun", true);

        if (firstRun) {
            firstTimeServerSetup(context);
            globalSettings.edit()
                    .putBoolean("firstrun", false)
                    .putBoolean("firstDbRun", false)
                    .apply();
        } else if (firstDbRun) {
            final List<File> fileList = SharedPreferencesUtils.getOldServers(context);
            migrateToDatabase(fileList, context);
            firstDbSetup(context);
            globalSettings.edit().putBoolean("firstDbRun", false).apply();
        }
    }

    private static List<File> getOldServers(final Context context) {
        final ArrayList<File> array = new ArrayList<>();
        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath(context));

        for (final File file : folder.listFiles()) {
            if (!SharedPreferencesUtils.isExcludedString(file.getName())) {
                array.add(file);
            }
        }
        return array;
    }

    private static void migrateToDatabase(final List<File> array, final Context context) {
        final ServerDatabase source = ServerDatabase.getInstance(context);
        for (final File file : array) {
            final String prefsName = file.getName().replace(".xml", "");
            // Get builder to transfer
            final ServerConfiguration.Builder builder = convertPrefsToBuilder(context, prefsName);
            // Only transfer if the builder is not broken
            if (StringUtils.isNotEmpty(builder.getTitle()) && StringUtils
                    .isNotEmpty(builder.getUrl())) {
                // Also transfer over ignore list
                final List<String> ignoreList = getIgnoreList(context, prefsName);
                source.addServer(builder, ignoreList);
            }
            file.delete();
        }
    }

    private static boolean isExcludedString(final String fileName) {
        return fileName.equals("main.xml") || fileName.contains("com.fusionx.lightirc") ||
                fileName.equals("showcase_internal.xml") || fileName.equals("tempUselessFile.xml");
    }

    private static void firstTimeServerSetup(final Context context) {
        final ServerDatabase source = ServerDatabase.getInstance(context);
        final List<ServerConfiguration.Builder> builders = BuilderUtils.getFirstTimeBuilderList();
        for (final ServerConfiguration.Builder builder : builders) {
            source.addServer(builder, new ArrayList<>());
        }
    }

    private static void firstDbSetup(final Context context) {
        final ServerDatabase source = ServerDatabase.getInstance(context);
        final List<ServerConfiguration.Builder> builders = BuilderUtils.getFirstTimeBuilderList();
        for (final ServerConfiguration.Builder builder : builders) {
            if (source.getBuilderByName(builder.getTitle()) == null) {
                source.addServer(builder, new ArrayList<>());
            }
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
        final ArrayList<String> auto = new ArrayList<String>(serverSettings
                .getStringSet(PreferenceConstants.PREF_AUTOJOIN, new HashSet<>()));
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

    private static List<String> getIgnoreList(Context context, String filename) {
        final SharedPreferences serverSettings = context.getSharedPreferences(filename,
                MODE_PRIVATE);
        final Set<String> ignoreSet = serverSettings.getStringSet(PREF_IGNORE_LIST,
                new HashSet<>());
        return new ArrayList<>(ignoreSet);
    }
}
