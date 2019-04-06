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

package app.holoirc.ui;

import app.holoirc.R;
import app.holoirc.misc.PreferenceConstants;
import app.holoirc.ui.helper.RestartAppPreferenceListener;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import static app.holoirc.misc.PreferenceConstants.FRAGMENT_SETTINGS_THEME;

public class AppearancePreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_settings_fragment);

        final PreferenceScreen screen = getPreferenceScreen();
        final ListPreference theme = (ListPreference) screen.findPreference
                (FRAGMENT_SETTINGS_THEME);
        if (theme.getEntry() == null) {
            theme.setValue("1");
        }
        theme.setOnPreferenceChangeListener(new RestartAppPreferenceListener(getActivity()));
        theme.setSummary(theme.getEntry());

        final ListPreference fontSize = (ListPreference) screen.findPreference
                (PreferenceConstants.PREF_MAIN_FONT_SIZE);
        fontSize.setOnPreferenceChangeListener((preference, newValue) -> {
            final CharSequence summary = fontSize.getEntries()[fontSize
                    .findIndexOfValue(String.valueOf(newValue))];
            fontSize.setSummary(summary);
            return true;
        });
        fontSize.setSummary(fontSize.getEntry());
    }
}