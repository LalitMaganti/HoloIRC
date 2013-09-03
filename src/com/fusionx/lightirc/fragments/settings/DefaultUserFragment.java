package com.fusionx.lightirc.fragments.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.fragments.PreferenceListFragment;

/**
 * KEEP THIS CODE SYNCED WITH THE CODE IN SettingsActivity
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DefaultUserFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.default_user_fragment);
    }
}