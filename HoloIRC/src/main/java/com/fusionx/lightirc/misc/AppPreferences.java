package com.fusionx.lightirc.misc;

import com.fusionx.relay.constants.Theme;
import com.fusionx.relay.interfaces.EventPreferences;

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
        final int themeInt = Integer.parseInt(preferences.getString(PreferenceConstants
                .FRAGMENT_SETTINGS_THEME, "1"));
        theme = themeInt != 0 ? Theme.LIGHT : Theme.DARK;
        highlightLine = preferences.getBoolean(PreferenceConstants.PREF_HIGHLIGHT_WHOLE_LINE, true);
        timestamp = preferences.getBoolean(PreferenceConstants.PREF_TIMESTAMPS, false);
        motdAllowed = preferences.getBoolean(PreferenceConstants.PREF_MOTD, true);
        hideUserMessages = preferences.getBoolean(PreferenceConstants.PREF_HIDE_MESSAGES, false);
        partReason = preferences.getString(PreferenceConstants.PREF_PART_REASON, "");
        quitReason = preferences.getString(PreferenceConstants.PREF_QUIT_REASON, "");
        numberOfReconnectEvents = preferences.getInt(PreferenceConstants.PREF_RECONNECT_TRIES, 3);
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
    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean shouldLogUserListChanges() {
        return !hideUserMessages;
    }

    // We always want to display the messages that the app user sends
    @Override
    public boolean isSelfEventBroadcast() {
        return true;
    }

    @Override
    public boolean isMOTDShown() {
        return motdAllowed;
    }

    @Override
    public boolean shouldHighlightLine() {
        return highlightLine;
    }

    @Override
    public boolean shouldNickBeColourful() {
        return true;
    }
}