package app.holoirc.ui;

import app.holoirc.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class DefaultUserPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.default_user_fragment);
    }
}