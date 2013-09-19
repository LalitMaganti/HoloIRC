package com.fusionx.lightirc.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.UIUtils;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        if (!UIUtils.hasHoneycomb()) {
            if (action != null) {
                if (action.equals(PREF_ACTION_APPEARANCE)) {// Appearance settings
                    addPreferencesFromResource(R.xml.appearance_settings_fragment);
                    AppearancePreferenceFragment.setupThemePreference(getPreferenceScreen(),
                            this);

                } else if (action.equals(PREF_ACTION_SERVER_CHANNEL)) {// Server Channel Settings
                    addPreferencesFromResource(R.xml.server_channel_settings_fragment);
                    ServerChannelPreferenceFragment.setupNumberPicker(getPreferenceScreen());

                } else if (action.equals(PREF_ACTION_DEFAULT_USER)) {// Default User Settings
                    addPreferencesFromResource(R.xml.default_user_fragment);

                } else if (action.equals(PREF_ACTION_NOTIFICATION)) {// Notification Settings
                    addPreferencesFromResource(R.xml.notification_settings);

                } else if (action.equals(PREF_ACTION_ABOUT)) {// About settings
                    addPreferencesFromResource(R.xml.about_settings_fragment);
                    AboutPreferenceFragment.setupAppVersionPreference(getPreferenceScreen(),
                            this);

                }
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Modifying these settings while connected to server can cause " +
                "unexpected behaviour - this is not a bug. It is strongly recommended that you " +
                "close any connections before modifying these settings.").setTitle("Warning")
                .setCancelable(false).setPositiveButton(getString(android.R.string.ok), null);
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
