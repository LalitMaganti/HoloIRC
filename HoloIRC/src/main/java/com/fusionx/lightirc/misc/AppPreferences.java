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


    private Theme mTheme;

    private boolean mTimestamp = false;

    private boolean mHideUserMessages = false;


    private boolean mHighlightLine = true;

    private boolean mMotdAllowed = true;


    private int mNumberOfReconnectEvents = 3;


    private String mPartReason = "";

    private String mQuitReason = "";

    // Notification settings
    private boolean mInAppNotification;

    private Set<String> mInAppNotificationSettings;

    private boolean mOutOfAppNotification;

    private Set<String> mOutOfAppNotificationSettings;

    // Logging
    private boolean mLoggingEnabled;

    private String mLoggingDirectory;

    private boolean mLoggingTimeStamp;

    // Font settings
    private int mMainFontSize;

    // Bug reporting
    private boolean mBugReportingEnabled;

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
        return mTheme;
    }

    public boolean shouldDisplayTimestamps() {
        return mTimestamp;
    }

    public boolean shouldHideUserMessages() {
        return mHideUserMessages;
    }

    public boolean shouldHighlightLine() {
        return mHighlightLine;
    }

    // Notification settings
    public boolean isInAppNotification() {
        return mInAppNotification;
    }

    public Set<String> getInAppNotificationSettings() {
        return mInAppNotificationSettings;
    }

    public boolean isOutOfAppNotification() {
        return mOutOfAppNotification;
    }

    public Set<String> getOutOfAppNotificationSettings() {
        return mOutOfAppNotificationSettings;
    }

    // Logging settings
    public boolean isLoggingEnabled() {
        return mLoggingEnabled;
    }

    // Font settings
    public int getMainFontSize() {
        return mMainFontSize;
    }

    // Bug reporting settings
    public boolean isBugReportingEnabled() {
        return mBugReportingEnabled;
    }

    /*
     * Interface implementation starts here
     */
    @Override
    public int getReconnectAttemptsCount() {
        return mNumberOfReconnectEvents;
    }

    @Override
    public String getPartReason() {
        return mPartReason;
    }

    @Override
    public String getQuitReason() {
        return mQuitReason;
    }

    // We always want to display the messages that the app user sends
    @Override
    public boolean isSelfEventBroadcast() {
        return true;
    }

    @Override
    public boolean isMOTDShown() {
        return mMotdAllowed;
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
        mTheme = themeInt != 0 ? Theme.LIGHT : Theme.DARK;
        mHighlightLine = preferences
                .getBoolean(PreferenceConstants.PREF_HIGHLIGHT_WHOLE_LINE, true);

        mTimestamp = preferences.getBoolean(PreferenceConstants.PREF_TIMESTAMPS, false);
        mMotdAllowed = preferences.getBoolean(PreferenceConstants.PREF_MOTD, true);
        mHideUserMessages = preferences
                .getBoolean(PreferenceConstants.PREF_HIDE_MESSAGES, false);
        mMainFontSize = Integer.parseInt(preferences.getString(PreferenceConstants
                .PREF_MAIN_FONT_SIZE, "14"));

        mNumberOfReconnectEvents = preferences
                .getInt(PreferenceConstants.PREF_RECONNECT_TRIES, 3);

        mPartReason = preferences.getString(PreferenceConstants.PREF_PART_REASON, "");
        mQuitReason = preferences.getString(PreferenceConstants.PREF_QUIT_REASON, "");

        // Notification settings
        mInAppNotificationSettings = preferences.getStringSet(PreferenceConstants
                .PREF_IN_APP_NOTIFICATION_SETTINGS, new HashSet<String>());
        mInAppNotification = preferences.getBoolean(PreferenceConstants
                .PREF_IN_APP_NOTIFICATION, true);
        mOutOfAppNotificationSettings = preferences.getStringSet(PreferenceConstants
                .PREF_OUT_OF_APP_NOTIFICATION_SETTINGS, new HashSet<String>());
        mOutOfAppNotification = preferences.getBoolean(PreferenceConstants
                .PREF_OUT_OF_APP_NOTIFICATION, true);

        // Logging
        mLoggingEnabled = preferences.getBoolean(PreferenceConstants.PREF_LOGGING, false);
        mLoggingDirectory = preferences.getString(PreferenceConstants.PREF_LOGGING_DIRECTORY,
                "/IRCLogs");
        mLoggingTimeStamp = preferences.getBoolean(PreferenceConstants.PREF_LOGGING_TIMESTAMP,
                true);

        // Bug reporting settings
        mBugReportingEnabled = preferences.getBoolean(PreferenceConstants.PREF_BUG_REPORTING, true);
    }
}