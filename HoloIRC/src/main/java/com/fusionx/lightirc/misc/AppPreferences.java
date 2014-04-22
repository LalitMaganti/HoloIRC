package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.relay.constants.Theme;
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

    private final Context mContext;

    private boolean timestamp = false;

    private boolean hideUserMessages = false;

    private Theme theme;

    private boolean inAppNotification;

    private Set<String> inAppNotificationSettings;

    private boolean outOfAppNotification;

    private Set<String> outOfAppNotificationSettings;

    private boolean highlightLine = true;

    private boolean motdAllowed = true;

    private String partReason = "";

    private String quitReason = "";

    private int numberOfReconnectEvents = 3;

    private SharedPreferences.OnSharedPreferenceChangeListener sPrefsChangeListener;

    // Logging
    private boolean mLoggingEnabled;

    private String mLoggingDirectory;

    private boolean mLoggingTimeStamp;

    public AppPreferences(final Context context) {
        mContext = context.getApplicationContext();
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

    public void setPreferences(final SharedPreferences preferences) {
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

        // Logging
        mLoggingEnabled = preferences.getBoolean(PreferenceConstants.PREF_LOGGING, false);
        mLoggingDirectory = preferences.getString(PreferenceConstants.PREF_LOGGING_DIRECTORY,
                "/IRCLogs");
        mLoggingTimeStamp = preferences.getBoolean(PreferenceConstants.PREF_LOGGING_TIMESTAMP,
                true);
    }

    public boolean isLoggingEnabled() {
        return mLoggingEnabled;
    }

    public String getLoggingRelativePath() {
        return mLoggingDirectory;
    }

    public boolean isTimestamp() {
        return timestamp;
    }

    public boolean isHideUserMessages() {
        return hideUserMessages;
    }

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

    @Override
    public boolean shouldLogTimestamps() {
        return mLoggingTimeStamp;
    }

    @Override
    public String getLoggingPath() {
        return Environment.getExternalStorageDirectory() + "/" + mLoggingDirectory;
    }

    public String getRelativeLoggingPath() {
        return mLoggingDirectory;
    }
}