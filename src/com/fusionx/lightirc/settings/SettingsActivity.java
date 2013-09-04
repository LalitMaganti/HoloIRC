package com.fusionx.lightirc.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.fusionx.lightirc.misc.PreferenceKeys;
import com.fusionx.lightirc.utils.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.uiircinterface.core.IRCBridgeService;
import com.michaelnovakjr.numberpicker.NumberPickerPreference;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements ISettings {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            final PreferenceScreen preferenceScreen = getPreferenceScreen();

            // Appearance settings
            addPreferencesFromResource(R.xml.appearance_settings_fragment);
            setupThemePreference(preferenceScreen);

            // Server Channel Settings
            addPreferencesFromResource(R.xml.server_channel_settings_fragment);
            setupNumberPicker(preferenceScreen);

            // Default User Settings
            addPreferencesFromResource(R.xml.default_user_fragment);

            // About settings
            addPreferencesFromResource(R.xml.about_settings_fragment);
            setupAppVersionPreference(preferenceScreen);

            showAlertDialog();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.main_settings_headers, target);
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

    @Override
    public void setupNumberPicker(final PreferenceScreen screen) {
        final NumberPickerPreference numberPickerDialogPreference = (NumberPickerPreference)
                screen.findPreference(PreferenceKeys.ReconnectTries);
        numberPickerDialogPreference.setSummary(String.valueOf(numberPickerDialogPreference
                .getCurrent()));
    }

    @Override
    public void setupThemePreference(final PreferenceScreen screen) {
        final String[] themes_entries = {"0", "1"};
        final ListPreference themePreference = (ListPreference) screen.findPreference
                (PreferenceKeys.Theme);
        themePreference.setEntryValues(themes_entries);
        if (themePreference.getEntry() == null) {
            themePreference.setValue("1");
        }
        themePreference.setOnPreferenceChangeListener(new ThemeChangeListener(this));
        themePreference.setSummary(themePreference.getEntry());
    }

    @Override
    public void setupAppVersionPreference(final PreferenceScreen screen) {
        final Preference appVersionPreference = screen.findPreference(PreferenceKeys
                .AppVersion);
        if (appVersionPreference != null) {
            appVersionPreference.setSummary(Utils.getAppVersion(this));
        }
    }

    static class ThemeChangeListener implements Preference.OnPreferenceChangeListener {
        private final Activity mActivity;
        ThemeChangeListener(final Activity context) {
            mActivity = context;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            final AlertDialog.Builder build = new AlertDialog.Builder(mActivity);
            build.setMessage(mActivity.getString(R.string.appearance_settings_requires_restart))
                    .setPositiveButton(mActivity.getString(R.string.restart),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final Intent service = new Intent(mActivity,
                                            IRCBridgeService.class);
                                    service.putExtra("stop", false);
                                    mActivity.bindService(service, mConnection, 0);
                                }
                            });
            build.show();
            return true;
        }

        private final ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder binder) {
                final IRCBridgeService service = ((IRCBridgeService.IRCBinder) binder).getService();
                service.disconnectAll();
                mActivity.unbindService(mConnection);
                final Intent intent = mActivity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(mActivity.getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            }
            @Override
            public void onServiceDisconnected(final ComponentName name) {
            }
        };
    }
}
