package com.fusionx.lightirc.fragments.serversettings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.ServerSettingsListenerInterface;
import com.fusionx.lightirc.misc.PreferenceKeys;

import java.util.ArrayList;
import java.util.List;

public class BaseServerSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private EditTextPreference mServerPassword = null;
    // NickServ
    private EditTextPreference mNickServPassword = null;

    private final List<EditTextPreference> mEditTexts = new ArrayList<EditTextPreference>();

    private ServerSettingsListenerInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ServerSettingsListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChannelFragmentListenerInterface");
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getPreferenceManager().setSharedPreferencesName(mListener.getFileName());

        addPreferencesFromResource(R.xml.activty_server_settings_prefs);

        final PreferenceScreen prefSet = getPreferenceScreen();

        EditTextPreference mEditTextTitle = (EditTextPreference) prefSet.findPreference(PreferenceKeys.Title);
        if (mEditTextTitle != null) {
            mEditTextTitle.setOnPreferenceChangeListener(this);
        }
        mEditTexts.add(mEditTextTitle);

        // URL of server
        EditTextPreference mEditTextUrl = (EditTextPreference) prefSet.findPreference(PreferenceKeys.URL);
        if (mEditTextUrl != null) {
            mEditTextUrl.setOnPreferenceChangeListener(this);
        }
        mEditTexts.add(mEditTextUrl);

        // Port of server
        EditTextPreference mEditTextPort = (EditTextPreference) prefSet.findPreference(PreferenceKeys.Port);
        if (mEditTextPort != null) {
            mEditTextPort.setOnPreferenceChangeListener(this);
        }
        mEditTexts.add(mEditTextPort);

        // Nick of User
        EditTextPreference mEditTextNick = (EditTextPreference) prefSet.findPreference(PreferenceKeys.Nick);
        if (mEditTextNick != null) {
            mEditTextNick.setOnPreferenceChangeListener(this);
        }
        mEditTexts.add(mEditTextNick);

        EditTextPreference mServerUserName = (EditTextPreference) prefSet.findPreference(PreferenceKeys.ServerUserName);
        if (mServerUserName != null) {
            mServerUserName.setOnPreferenceChangeListener(this);
        }

        mServerPassword = (EditTextPreference) prefSet.findPreference(PreferenceKeys.ServerPassword);
        if (mServerPassword != null) {
            mServerPassword.setOnPreferenceChangeListener(this);
        }

        mNickServPassword = (EditTextPreference) prefSet.findPreference(PreferenceKeys.NickServPassword);
        if (mNickServPassword != null) {
            mNickServPassword.setOnPreferenceChangeListener(this);
        }

        if (mListener.getNewServer()) {
            // Title of server
            mEditTextTitle.setSummary(getString(R.string.server_settings_not_empty));

            // URL of server
            mEditTextUrl.setSummary(getString(R.string.server_settings_not_empty));

            // Port of server
            mEditTextPort.setSummary(getString(R.string.server_settings_not_empty_port));

            // Nick of User
            mEditTextNick.setSummary(getString(R.string.server_settings_not_empty));
        } else {
            for (EditTextPreference edit : mEditTexts) {
                edit.setSummary(edit.getText());
            }
        }

        // Server username
        if (mServerUserName != null) {
            mServerUserName.setSummary(mServerUserName.getText());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue instanceof String) {
            final String newString = (String) newValue;
            if (preference != mNickServPassword && preference != mServerPassword) {
                preference.setSummary(newString);
            }
        }
        if (mListener.getNewServer()) {
            mListener.setCanExit(true);
            for (EditTextPreference edit : mEditTexts) {
                if (edit.getText() == null) {
                    mListener.setCanExit(false);
                    break;
                }
            }
        }
        return true;
    }
}