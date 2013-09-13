package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fusionx.lightirc.constants.PreferenceConstants;

public class AppPreferences {
    public static boolean highlightLine = true;

    public static void setUpPreferences(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        updateLineColourfulness(preferences);
    }

    public static void updateLineColourfulness(final SharedPreferences preferences) {
        highlightLine = preferences.getBoolean(PreferenceConstants.LineColourful, true);
    }
}