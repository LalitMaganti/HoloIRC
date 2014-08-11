package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.app_settings_headers, target);
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        return true;
    }
}
