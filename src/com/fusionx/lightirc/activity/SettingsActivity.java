package com.fusionx.lightirc.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.fusionx.lightirc.fragments.SettingsFragment;
import com.fusionx.lightirc.misc.Utils;

public class SettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(getApplicationContext()));
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}