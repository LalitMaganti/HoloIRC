package com.fusionx.lightirc.fragments.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceKeys;
import com.fusionx.lightirc.uisubclasses.LightEditTextPreference;

public class ServerChannelSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_channel_settings_fragment);

        final LightEditTextPreference partReason = (LightEditTextPreference) getPreferenceScreen()
                .findPreference(PreferenceKeys.PartReason);
        partReason.setOnPreferenceChangeListener(this);
        partReason.setSummary(partReason.getText());
        final LightEditTextPreference quitReason = (LightEditTextPreference) getPreferenceScreen()
                .findPreference(PreferenceKeys.QuitReason);
        quitReason.setOnPreferenceChangeListener(this);
        quitReason.setSummary(quitReason.getText());
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        final LightEditTextPreference editTextPreference = (LightEditTextPreference) preference;
        editTextPreference.setSummary((CharSequence) o);
        return true;
    }
}
