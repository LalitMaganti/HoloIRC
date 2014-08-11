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

package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceConstants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class AppearancePreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_settings_fragment);

        final PreferenceScreen screen = getPreferenceScreen();
        final ListPreference themePreference = (ListPreference) screen.findPreference
                (PreferenceConstants.FRAGMENT_SETTINGS_THEME);
        if (themePreference.getEntry() == null) {
            themePreference.setValue("1");
        }
        themePreference.setOnPreferenceChangeListener(new ThemePreferenceListener());
        themePreference.setSummary(themePreference.getEntry());

        final ListPreference fontSize = (ListPreference) screen.findPreference
                (PreferenceConstants.PREF_MAIN_FONT_SIZE);
        if (fontSize != null) {
            fontSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final CharSequence summary = fontSize.getEntries()[fontSize
                            .findIndexOfValue(String.valueOf(newValue))];
                    fontSize.setSummary(summary);
                    return true;
                }
            });
            fontSize.setSummary(fontSize.getEntry());
        }
    }

    private class ThemePreferenceListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(final Preference preference, final Object o) {
            final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final Intent intent = new Intent(getActivity(),
                            MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(MainActivity.CLEAR_CACHE, true);
                    getActivity().startActivity(intent);
                }
            };

            build.setMessage(getString(R.string.appearance_settings_requires_restart))
                    .setPositiveButton(getString(R.string.restart), listener)
                    .show();
            return true;
        }
    }
}