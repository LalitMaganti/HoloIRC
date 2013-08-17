package com.fusionx.lightirc.fragments.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceKeys;
import com.fusionx.lightlibrary.ui.NonEmptyEditTextPreference;

public class ServerChannelSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_channel_settings_fragment);

        final NonEmptyEditTextPreference partReason = (NonEmptyEditTextPreference) getPreferenceScreen()
                .findPreference(PreferenceKeys.PartReason);
        partReason.setOnPreferenceChangeListener(this);
        partReason.setSummary(partReason.getText());
        final NonEmptyEditTextPreference quitReason = (NonEmptyEditTextPreference) getPreferenceScreen()
                .findPreference(PreferenceKeys.QuitReason);
        quitReason.setOnPreferenceChangeListener(this);
        quitReason.setSummary(quitReason.getText());
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        final NonEmptyEditTextPreference editTextPreference = (NonEmptyEditTextPreference) preference;
        editTextPreference.setSummary((CharSequence) o);
        return true;
    }
}
