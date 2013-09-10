package com.fusionx.lightirc.ui;

import android.os.Bundle;

import com.fusionx.lightirc.R;

import org.holoeverywhere.preference.PreferenceFragment;

public class DefaultUserPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.default_user_fragment);
    }
}