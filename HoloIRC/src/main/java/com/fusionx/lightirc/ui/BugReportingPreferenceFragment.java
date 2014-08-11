package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class BugReportingPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_settings_fragment);
    }
}