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
