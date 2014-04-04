package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.util.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;

public class ChannelListActivity extends ActionBarActivity implements ServerSettingsCallbacks {

    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);

        mFileName = getIntent().getStringExtra("filename");

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                new ChannelListFragment()).commit();
    }

    @Override
    public void setupPreferences(PreferenceScreen screen, Activity activity) {
        throw new IllegalArgumentException();
    }

    public boolean onPreferenceChange(Preference preference) {
        throw new IllegalArgumentException();
    }
}