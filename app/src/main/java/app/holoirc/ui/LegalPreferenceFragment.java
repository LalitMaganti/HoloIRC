package app.holoirc.ui;

import app.holoirc.R;
import app.holoirc.misc.PreferenceConstants;
import app.holoirc.util.MiscUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class LegalPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_legal_fragment);

        final PreferenceScreen screen = getPreferenceScreen();
        final Context context = getActivity();

        final Preference privacy = screen.findPreference(PreferenceConstants.PREF_LEGAL_PRIVACY);
        if (privacy != null) {
            privacy.setOnPreferenceClickListener(preference -> {
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://holoirc.com/legal/privacy/"));
                context.startActivity(browserIntent);
                return true;
            });
        }
    }

}
