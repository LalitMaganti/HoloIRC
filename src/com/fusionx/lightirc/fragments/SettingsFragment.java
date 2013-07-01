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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mChooseTheme) {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setMessage("This requires a full restart of the app")
                    .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
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
