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

import java.util.HashMap;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private ListPreference mChooseTheme;
    private HashMap<Integer, String> themesMap = new HashMap<Integer, String>();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);

        String[] mTempArray = getResources().getStringArray(R.array.themes_entries);
        String[] mTempThemes = getResources().getStringArray(R.array.themes);
        for (int i = 0; i < mTempArray.length; i++) {
            themesMap.put(Integer.parseInt(mTempArray[i]), mTempThemes[i]);
        }

        final PreferenceScreen prefSet = getPreferenceScreen();
        mChooseTheme = (ListPreference) prefSet.findPreference("fragment_settings_theme");
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
