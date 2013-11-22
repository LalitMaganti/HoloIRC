package com.fusionx.lightirc.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fusionx.lightirc.constants.PreferenceConstants;

public class AppPreferences {
    public static boolean highlightLine = true;
    public static boolean timestamp = false;
    public static boolean motdAllowed = true;
    public static boolean hideUserMessages = false;
    public static String partReason = "";
    public static String quitReason = "";
    public static int numberOfReconnectEvents = 3;

    public static void setUpPreferences(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (context);
        highlightLine = preferences.getBoolean(PreferenceConstants.LineColourful, true);
        timestamp = preferences.getBoolean(PreferenceConstants.Timestamp, false);
        motdAllowed = preferences.getBoolean(PreferenceConstants.Motd, true);
        hideUserMessages = preferences.getBoolean(PreferenceConstants.HideMessages, false);
        partReason = preferences.getString(PreferenceConstants.PartReason, "");
        quitReason = preferences.getString(PreferenceConstants.QuitReason, "");
        numberOfReconnectEvents = preferences.getInt(PreferenceConstants.ReconnectTries, 3);
    }
}