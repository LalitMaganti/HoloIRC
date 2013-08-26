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

import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.fusionx.common.PreferenceKeys;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.preferences.NumberPickerDialogPreference;

public class ServerChannelSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_channel_settings_fragment);

        final NumberPickerDialogPreference numberPickerDialogPreference =
                (NumberPickerDialogPreference) getPreferenceScreen().findPreference
                        (PreferenceKeys.ReconnectTries);
        numberPickerDialogPreference.setOnPreferenceChangeListener(this);
        numberPickerDialogPreference.setSummary(String.valueOf(numberPickerDialogPreference
                .getValue()));

        final EditTextPreference partReason = (EditTextPreference) getPreferenceScreen()
                .findPreference(PreferenceKeys.PartReason);
        partReason.setOnPreferenceChangeListener(this);
        partReason.setSummary(partReason.getText());

        final EditTextPreference quitReason = (EditTextPreference) getPreferenceScreen()
                .findPreference(PreferenceKeys.QuitReason);
        quitReason.setOnPreferenceChangeListener(this);
        quitReason.setSummary(quitReason.getText());
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        final DialogPreference editTextPreference = (DialogPreference) preference;
        editTextPreference.setSummary(String.valueOf(o));
        return true;
    }
}