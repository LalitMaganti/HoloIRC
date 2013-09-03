/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.fusionx.common.PreferenceKeys;
import com.fusionx.common.utils.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.fragments.PreferenceListFragment;
import com.fusionx.uiircinterface.core.IRCBridgeService;
import com.michaelnovakjr.numberpicker.NumberPickerPreference;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements PreferenceListFragment
        .OnPreferenceAttachedListener, Preference.OnPreferenceChangeListener {
    private ListPreference mChooseTheme = null;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.appearance_settings_fragment);
            addPreferencesFromResource(R.xml.server_channel_settings_fragment);
            addPreferencesFromResource(R.xml.default_user_fragment);
            addPreferencesFromResource(R.xml.about_settings_fragment);

            final Preference appVersionPreference = getPreferenceScreen().findPreference
                    (PreferenceKeys.AppVersion);
            if (appVersionPreference != null) {
                appVersionPreference.setSummary(Utils.getAppVersion(this));
            }

            final NumberPickerPreference numberPickerDialogPreference =
                    (NumberPickerPreference) getPreferenceScreen().findPreference
                            (PreferenceKeys.ReconnectTries);
            numberPickerDialogPreference.setOnPreferenceChangeListener(this);
            numberPickerDialogPreference.setSummary(String.valueOf(numberPickerDialogPreference
                    .getCurrent()));

            final EditTextPreference partReason = (EditTextPreference) getPreferenceScreen()
                    .findPreference(PreferenceKeys.PartReason);
            partReason.setOnPreferenceChangeListener(this);
            partReason.setSummary(partReason.getText());

            final EditTextPreference quitReason = (EditTextPreference) getPreferenceScreen()
                    .findPreference(PreferenceKeys.QuitReason);
            quitReason.setOnPreferenceChangeListener(this);
            quitReason.setSummary(quitReason.getText());

            final String[] themes_entries = {"0", "1"};

            final PreferenceScreen prefSet = getPreferenceScreen();
            mChooseTheme = (ListPreference) prefSet.findPreference(PreferenceKeys.Theme);
            mChooseTheme.setEntryValues(themes_entries);
            if (mChooseTheme.getEntry() == null) {
                mChooseTheme.setValue("1");
            }
            mChooseTheme.setOnPreferenceChangeListener(this);
            mChooseTheme.setSummary(mChooseTheme.getEntry());

            showAlertDialog();
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (preference == mChooseTheme) {
            final AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setMessage(getString(R.string.appearance_settings_requires_restart))
                    .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Intent service = new Intent(SettingsActivity.this,
                                    IRCBridgeService.class);
                            service.putExtra("stop", false);
                            bindService(service, mConnection, 0);
                        }
                    });
            build.show();
        } else {
            final DialogPreference editTextPreference = (DialogPreference) preference;
            editTextPreference.setSummary(String.valueOf(o));
        }
        return true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            final IRCBridgeService service = ((IRCBridgeService.IRCBinder) binder).getService();
            service.disconnectAll();
            unbindService(mConnection);
            final Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
        }
    };

    private void showAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Modifying these settings while connected to server can cause " +
                "unexpected behaviour - this is not a bug. It is strongly recommended that you " +
                "close any connections before modifying these settings.").setTitle("Warning")
                .setCancelable(false).setPositiveButton(getString(android.R.string.ok), null);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(final List<Header> target) {
            loadHeadersFromResource(R.xml.main_settings_headers, target);
            showAlertDialog();
    }

    @Override
    public void onPreferenceAttached(PreferenceScreen root, int xmlId) {

    }
}