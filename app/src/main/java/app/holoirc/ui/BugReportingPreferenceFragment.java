package app.holoirc.ui;

import app.holoirc.R;
import app.holoirc.ui.helper.RestartAppPreferenceListener;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import static app.holoirc.misc.PreferenceConstants.PREF_BUG_REPORTING;

public class BugReportingPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bug_reporting_preference_fragment);

        final Preference preference = getPreferenceScreen().findPreference(PREF_BUG_REPORTING);
        preference.setOnPreferenceChangeListener(new RestartAppPreferenceListener(getActivity()));
    }
}