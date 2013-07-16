package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class SharedPreferencesUtils {
    public static String getSharedPreferencesPath(final Context applicationContext) {
        return applicationContext.getFilesDir().getAbsolutePath().replace("files", "shared_prefs/");
    }

    public static void firstTimeServerSetup(final Context context) {
        final SharedPreferences settings = context.getSharedPreferences("server_0", Context.MODE_PRIVATE);
        final SharedPreferences.Editor e = settings.edit();

        e.putString(PreferenceKeys.Title, "Freenode");
        e.putString(PreferenceKeys.URL, "irc.freenode.net");
        e.putString(PreferenceKeys.Port, "6667");
        e.putBoolean(PreferenceKeys.SSL, false);

        e.putString(PreferenceKeys.Nick, "LightIRCUser");
        e.putBoolean(PreferenceKeys.AutoNickChange, true);

        e.putString(PreferenceKeys.ServerUserName, "lightirc");

        final HashSet<String> auto = new HashSet<>();
        e.putStringSet(PreferenceKeys.AutoJoin, auto);
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
