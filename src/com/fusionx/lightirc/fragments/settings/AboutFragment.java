package com.fusionx.lightirc.fragments.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceKeys;

public class AboutFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_settings_fragment);

        final Preference appVersionPreference = getPreferenceScreen().findPreference(PreferenceKeys.AppVersion);
        final PackageManager manager = getActivity().getPackageManager();
        try {
            final PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            final String version = info.versionName;
            if (appVersionPreference != null) {
                appVersionPreference.setSummary(version);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
