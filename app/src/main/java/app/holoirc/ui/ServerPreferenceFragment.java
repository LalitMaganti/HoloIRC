package app.holoirc.ui;

import app.holoirc.R;
import app.holoirc.interfaces.ServerSettingsCallbacks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ServerPreferenceFragment extends PreferenceFragment {

    private ServerSettingsCallbacks mCallback = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (ServerSettingsCallbacks) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        getPreferenceManager().setSharedPreferencesName("tempUselessFile");

        addPreferencesFromResource(R.xml.activty_server_settings_prefs);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCallback.setupPreferences(getPreferenceScreen(), getActivity());
    }
}