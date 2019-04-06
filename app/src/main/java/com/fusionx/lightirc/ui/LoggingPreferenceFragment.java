package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceConstants;
import com.fusionx.lightirc.util.PreferenceUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LoggingPreferenceFragment extends PreferenceFragment {

    private List<Preference> mPreferenceList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.logging_settings);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        final boolean logging = preferences.getBoolean(PreferenceConstants.PREF_LOGGING, false);

        mPreferenceList = new ArrayList<>();
        PreferenceUtils.getPreferenceList(getPreferenceScreen(), mPreferenceList);

        updatePreferencesEnabled(logging);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_logging_ab, menu);

        final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        final boolean logging = preferences.getBoolean(PreferenceConstants.PREF_LOGGING, false);

        final View actionView = MenuItemCompat.getActionView(menu.findItem(R.id.logging_switch));
        final SwitchCompat logSwitch = (SwitchCompat) actionView.findViewById(R.id.logging_switch_view);

        logSwitch.setChecked(logging);
        logSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final SharedPreferences.Editor editor = getPreferenceManager()
                    .getSharedPreferences().edit();
            editor.putBoolean(PreferenceConstants.PREF_LOGGING, isChecked).commit();
            updatePreferencesEnabled(logSwitch.isChecked());
        });
    }

    private void updatePreferencesEnabled(final boolean loggingEnabled) {
        for (final Preference preference : mPreferenceList) {
            preference.setEnabled(loggingEnabled);
        }
    }
}