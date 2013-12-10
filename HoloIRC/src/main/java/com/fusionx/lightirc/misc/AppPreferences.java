package com.fusionx.lightirc.misc;

import com.fusionx.androidirclibrary.constants.Theme;
import com.fusionx.androidirclibrary.interfaces.EventPreferences;
import com.fusionx.lightirc.constants.PreferenceConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences implements EventPreferences {

    public static boolean highlightLine = true;

    public static boolean timestamp = false;

    public static boolean motdAllowed = true;

    public static boolean hideUserMessages = false;

    public static String partReason = "";

    public static String quitReason = "";

    public static int numberOfReconnectEvents = 3;

    public static Theme theme;

    public static void setUpPreferences(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (context);
        final int themeInt = Integer.parseInt(preferences.getString(PreferenceConstants.Theme,
                "1"));
        theme = themeInt != 0 ? Theme.LIGHT : Theme.DARK;
        highlightLine = preferences.getBoolean(PreferenceConstants.LineColourful, true);
        timestamp = preferences.getBoolean(PreferenceConstants.Timestamp, false);
        motdAllowed = preferences.getBoolean(PreferenceConstants.Motd, true);
        hideUserMessages = preferences.getBoolean(PreferenceConstants.HideMessages, false);
        partReason = preferences.getString(PreferenceConstants.PartReason, "");
        quitReason = preferences.getString(PreferenceConstants.QuitReason, "");
        numberOfReconnectEvents = preferences.getInt(PreferenceConstants.ReconnectTries, 3);
    }

    @Override
    public int getReconnectAttemptsCount() {
        return numberOfReconnectEvents;
    }

    @Override
    public String getPartReason() {
        return partReason;
    }

    @Override
    public String getQuitReason() {
        return quitReason;
    }

    @Override
    public boolean getShouldTimestampMessages() {
        return timestamp;
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    // TODO - this is broken - fixit
    @Override
    public boolean shouldIgnoreUser(String nick) {
        return false;
    }

    @Override
    public boolean shouldLogUserListChanges() {
        return !hideUserMessages;
    }

    // We always want to display the messages that the app user sends
    @Override
    public boolean shouldSendSelfMessageEvent() {
        return true;
    }
}