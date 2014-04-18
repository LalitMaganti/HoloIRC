package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.relay.constants.Theme;
import com.fusionx.relay.interfaces.EventPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class AppPreferences implements EventPreferences {

    private static boolean highlightLine = true;

    public static boolean timestamp = false;

    private static boolean motdAllowed = true;

    public static boolean hideUserMessages = false;

    private static String partReason = "";

    private static String quitReason = "";

    private static int numberOfReconnectEvents = 3;

    public static Theme theme;

    public static boolean inAppNotification;

    public static Set<String> inAppNotificationSettings;

    public static boolean outOfAppNotification;

    public static Set<String> outOfAppNotificationSettings;

    private static SharedPreferences.OnSharedPreferenceChangeListener sPrefsChangeListener;

    public static void setUpPreferences(final Context context) {
        if (sPrefsChangeListener == null) {
            sPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(final SharedPreferences
                        sharedPreferences, final String key) {
                    setPreferences(sharedPreferences);
                    EventBus.getDefault().post(new OnPreferencesChangedEvent());
                }
            };
            final SharedPreferences preferences = getDefaultSharedPreferences(context);
            preferences.registerOnSharedPreferenceChangeListener(sPrefsChangeListener);
            setPreferences(preferences);
        }
    }

    public static void setPreferences(final SharedPreferences preferences) {
        final int themeInt = Integer.parseInt(preferences.getString(PreferenceConstants
                .FRAGMENT_SETTINGS_THEME, "1"));
        theme = themeInt != 0 ? Theme.LIGHT : Theme.DARK;
        highlightLine = preferences
                .getBoolean(PreferenceConstants.PREF_HIGHLIGHT_WHOLE_LINE, true);
        timestamp = preferences.getBoolean(PreferenceConstants.PREF_TIMESTAMPS, false);
        motdAllowed = preferences.getBoolean(PreferenceConstants.PREF_MOTD, true);
        hideUserMessages = preferences
                .getBoolean(PreferenceConstants.PREF_HIDE_MESSAGES, false);
        partReason = preferences.getString(PreferenceConstants.PREF_PART_REASON, "");
        quitReason = preferences.getString(PreferenceConstants.PREF_QUIT_REASON, "");
        numberOfReconnectEvents = preferences
                .getInt(PreferenceConstants.PREF_RECONNECT_TRIES, 3);
        inAppNotificationSettings = preferences.getStringSet(PreferenceConstants
                .PREF_IN_APP_NOTIFICATION_SETTINGS, new HashSet<String>());
        inAppNotification = preferences.getBoolean(PreferenceConstants
                .PREF_IN_APP_NOTIFICATION, true);
        outOfAppNotificationSettings = preferences.getStringSet(PreferenceConstants
                .PREF_OUT_OF_APP_NOTIFICATION_SETTINGS, new HashSet<String>());
        outOfAppNotification = preferences.getBoolean(PreferenceConstants
                .PREF_OUT_OF_APP_NOTIFICATION, true);
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