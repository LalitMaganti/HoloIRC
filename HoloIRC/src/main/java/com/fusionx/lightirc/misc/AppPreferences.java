package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.relay.interfaces.EventPreferences;
import com.fusionx.relay.logging.LoggingPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.util.HashSet;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class AppPreferences implements EventPreferences, LoggingPreferences {

    private static AppPreferences mAppPreferences;

    private final SharedPreferences.OnSharedPreferenceChangeListener sPrefsChangeListener;


    private Theme theme;

    private boolean timestamp = false;

    private boolean hideUserMessages = false;


    private boolean highlightLine = true;

    private boolean motdAllowed = true;


    private int numberOfReconnectEvents = 3;


    private String partReason = "";

    private String quitReason = "";

    // Notification settings
    private boolean inAppNotification;

    private Set<String> inAppNotificationSettings;

    private boolean outOfAppNotification;

    private Set<String> outOfAppNotificationSettings;

    // Logging
    private boolean mLoggingEnabled;

    private String mLoggingDirectory;

    private boolean mLoggingTimeStamp;

    // Font settings
    private int mMainFontSize;

    private AppPreferences(final Context context) {
        sPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(final SharedPreferences
                    sharedPreferences, final String key) {
                setPreferences(sharedPreferences);
                getBus().post(new OnPreferencesChangedEvent());
            }
        };
        final SharedPreferences preferences = getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(sPrefsChangeListener);
        setPreferences(preferences);
    }

    public static void setupAppPreferences(final Context context) {
        if (mAppPreferences == null) {
            mAppPreferences = new AppPreferences(context);
        }
    }

    public static AppPreferences getAppPreferences() {
        return mAppPreferences;
    }

    public Theme getTheme() {
        return theme;
    }

    public boolean shouldDisplayTimestamps() {
        return timestamp;
    }

    public boolean shouldHideUserMessages() {
        return hideUserMessages;
    }

    public boolean shouldHighlightLine() {
        return highlightLine;
    }

    // Notification settings
    public boolean isInAppNotification() {
        return inAppNotification;
    }

    public Set<String> getInAppNotificationSettings() {
        return inAppNotificationSettings;
    }

    public boolean isOutOfAppNotification() {
        return outOfAppNotification;
    }

    public Set<String> getOutOfAppNotificationSettings() {
        return outOfAppNotificationSettings;
    }

    // Logging settings
    public boolean isLoggingEnabled() {
        return mLoggingEnabled;
    }

    // Font settings
    public int getMainFontSize() {
        return mMainFontSize;
    }

    /*
     * Interface implementation starts here
     */
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

    // We always want to display the messages that the app user sends
    @Override
    public boolean isSelfEventBroadcast() {
        return true;
    }

    @Override
    public boolean isMOTDShown() {
        return motdAllowed;
    }

    // Logging
    @Override
    public boolean shouldLogTimestamps() {
        return mLoggingTimeStamp;
    }

    @Override
    public String getLoggingPath() {
        return Environment.getExternalStorageDirectory() + "/" + mLoggingDirectory;
    }

    private void setPreferences(final SharedPreferences preferences) {
        final int themeInt = Integer.parseInt(preferences.getString(PreferenceConstants
                .FRAGMENT_SETTINGS_THEME, "1"));
        theme = themeInt != 0 ? Theme.LIGHT : Theme.DARK;
        highlightLine = preferences
                .getBoolean(PreferenceConstants.PREF_HIGHLIGHT_WHOLE_LINE, true);

        timestamp = preferences.getBoolean(PreferenceConstants.PREF_TIMESTAMPS, false);
        motdAllowed = preferences.getBoolean(PreferenceConstants.PREF_MOTD, true);
        hideUserMessages = preferences
                .getBoolean(PreferenceConstants.PREF_HIDE_MESSAGES, false);

        numberOfReconnectEvents = preferences
                .getInt(PreferenceConstants.PREF_RECONNECT_TRIES, 3);

        partReason = preferences.getString(PreferenceConstants.PREF_PART_REASON, "");
        quitReason = preferences.getString(PreferenceConstants.PREF_QUIT_REASON, "");

        // Notification settings
        inAppNotificationSettings = preferences.getStringSet(PreferenceConstants
                .PREF_IN_APP_NOTIFICATION_SETTINGS, new HashSet<String>());
        inAppNotification = preferences.getBoolean(PreferenceConstants
                .PREF_IN_APP_NOTIFICATION, true);
        outOfAppNotificationSettings = preferences.getStringSet(PreferenceConstants
                .PREF_OUT_OF_APP_NOTIFICATION_SETTINGS, new HashSet<String>());
        outOfAppNotification = preferences.getBoolean(PreferenceConstants
                .PREF_OUT_OF_APP_NOTIFICATION, true);

        // Logging
        mLoggingEnabled = preferences.getBoolean(PreferenceConstants.PREF_LOGGING, false);
        mLoggingDirectory = preferences.getString(PreferenceConstants.PREF_LOGGING_DIRECTORY,
                "/IRCLogs");
        mLoggingTimeStamp = preferences.getBoolean(PreferenceConstants.PREF_LOGGING_TIMESTAMP,
                true);

        // Font settings
        mMainFontSize = Integer.parseInt(preferences.getString(PreferenceConstants
                .PREF_MAIN_FONT_SIZE, "14"));
    }
}