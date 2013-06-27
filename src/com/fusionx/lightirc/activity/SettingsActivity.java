package com.fusionx.lightirc.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.fusionx.lightirc.fragments.SettingsFragment;

public class SettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(Integer.parseInt(prefs.getString("fragment_settings_theme", "16974105")));

        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}