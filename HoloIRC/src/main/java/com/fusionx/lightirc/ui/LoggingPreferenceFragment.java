package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class LoggingPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.logging_settings);
    }
}