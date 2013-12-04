package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.UIUtils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

public class AppPreferenceActivity extends PreferenceActivity {

    private final static String PREF_ACTION_APPEARANCE = "com.fusionx.lightirc.ui" +
            ".AppPreferenceActivity.Appearance";

    private final static String PREF_ACTION_SERVER_CHANNEL = "com.fusionx.lightirc.ui" +
            ".AppPreferenceActivity.ServerChannel";

    private final static String PREF_ACTION_DEFAULT_USER = "com.fusionx.lightirc.ui" +
            ".AppPreferenceActivity.DefaultUser";

    private final static String PREF_ACTION_NOTIFICATION = "com.fusionx.lightirc.ui" +
            ".AppPreferenceActivity.Notification";

    private final static String PREF_ACTION_ABOUT = "com.fusionx.lightirc.ui" +
            ".AppPreferenceActivity.About";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);

        if (!UIUtils.hasHoneycomb()) {
            final String action = getIntent().getAction();
            if (PREF_ACTION_APPEARANCE.equals(action)) {
                // Appearance settings
                addPreferencesFromResource(R.xml.appearance_settings_fragment);
                PreferenceHelpers.setupThemePreference(getPreferenceScreen(),
                        this);
            } else if (PREF_ACTION_SERVER_CHANNEL.equals(action)) {
                // Server Channel Settings
                addPreferencesFromResource(R.xml.server_channel_settings_fragment);
                PreferenceHelpers.setupNumberPicker(getPreferenceScreen());
            } else if (PREF_ACTION_DEFAULT_USER.equals(action)) {
                // Default User Settings
                addPreferencesFromResource(R.xml.default_user_fragment);
            } else if (PREF_ACTION_NOTIFICATION.equals(action)) {
                // Notification Settings
                addPreferencesFromResource(R.xml.notification_settings);
            } else if (PREF_ACTION_ABOUT.equals(action)) {
                // About settings
                addPreferencesFromResource(R.xml.about_settings_fragment);
                PreferenceHelpers.setupAppVersionPreference(getPreferenceScreen(),
                        this);
            } else {
                addPreferencesFromResource(R.xml.app_settings_headers_legacy);
                showAlertDialog();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        AppPreferences.setUpPreferences(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.app_settings_headers, target);
        showAlertDialog();
    }

    private void showAlertDialog() {
        if(getIntent().getIntExtra("connectedServers", 0) != 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Modifying these settings while connected to server can cause " +
                    "unexpected behaviour - this is not a bug. It is strongly recommended that you " +
                    "close all connections before modifying these settings.").setTitle("Warning")
                    .setCancelable(false).setPositiveButton(getString(android.R.string.ok), null);
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
