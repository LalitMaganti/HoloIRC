package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.preferences.MultiSelectListPreference;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceFragment;

import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_IN_APP_NOTIFICATION_SETTINGS;
import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_OUT_OF_APP_NOTIFICATION_SETTINGS;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NotificationPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notification_settings);
        setHasOptionsMenu(true);

        final Vibrator vibrator = (Vibrator) getActivity()
                .getSystemService(Context.VIBRATOR_SERVICE);
        if (!vibrator.hasVibrator()) {
            final MultiSelectListPreference inAppSettings = (MultiSelectListPreference)
                    findPreference(PREF_IN_APP_NOTIFICATION_SETTINGS);
            final MultiSelectListPreference outOfAppSettings = (MultiSelectListPreference)
                    findPreference(PREF_OUT_OF_APP_NOTIFICATION_SETTINGS);

            inAppSettings.setEntries(R.array.notification_in_entries_no_vibrator);
            inAppSettings.setEntryValues(R.array.notification_in_values_no_vibrator);
            outOfAppSettings.setEntries(R.array.notification_out_entries_no_vibrator);
            outOfAppSettings.setEntryValues(R.array.notification_out_values_no_vibrator);
        }
    }
}