package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.fusionx.lightirc.R;

public class Utils {
    public static int getThemeInt(final Context applicationContext) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return Integer.parseInt(prefs.getString("fragment_settings_theme", String.valueOf(R.style.Light)));
    }
}