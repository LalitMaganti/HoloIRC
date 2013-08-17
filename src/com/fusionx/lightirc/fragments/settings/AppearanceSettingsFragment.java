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

package com.fusionx.lightirc.fragments.settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.fusionx.uiircinterface.IRCBridgeService;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceKeys;

public class AppearanceSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private ListPreference mChooseTheme = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_settings_fragment);

        final String[] themes_entries = {String.valueOf(R.style.Dark), String.valueOf(R.style.Light)};

        final PreferenceScreen prefSet = getPreferenceScreen();
        mChooseTheme = (ListPreference) prefSet.findPreference(PreferenceKeys.Theme);
        mChooseTheme.setEntryValues(themes_entries);
        if (mChooseTheme.getEntry() == null) {
            mChooseTheme.setValue(String.valueOf(R.style.Light));
        }
        mChooseTheme.setOnPreferenceChangeListener(this);
        mChooseTheme.setSummary(mChooseTheme.getEntry());
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (preference == mChooseTheme) {
            final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setMessage(getString(R.string.appearance_settings_requires_restart))
                    .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Intent service = new Intent(getActivity(), IRCBridgeService.class);
                            service.putExtra("stop", false);
                            getActivity().bindService(service, mConnection, 0);
                        }
                    });
            build.show();
        }
        return true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            final IRCBridgeService service = ((IRCBridgeService.IRCBinder) binder).getService();
            service.disconnectAll();
            getActivity().unbindService(mConnection);
            final Intent intent = getActivity().getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
        }
    };
}