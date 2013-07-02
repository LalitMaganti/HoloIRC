/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.fusionx.lightirc.R;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private ListPreference mChooseTheme;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);

        final String[] themes_entries = {String.valueOf(R.style.Dark), String.valueOf(R.style.Light)};

        final PreferenceScreen prefSet = getPreferenceScreen();
        mChooseTheme = (ListPreference) prefSet.findPreference("fragment_settings_theme");
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
            build.setMessage(getString(R.string.settings_requires_restart))
                    .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = getActivity().getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
            build.show();
        }
        return true;
    }
}
