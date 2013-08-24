package com.fusionx.lightirc.fragments.serversetttings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.ui.MustBeCompleteView;
import com.fusionx.lightirc.ui.preferences.ServerTitleEditTextPreference;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.fusionx.common.PreferenceKeys.NickServPassword;
import static com.fusionx.common.PreferenceKeys.Port;
import static com.fusionx.common.PreferenceKeys.RealName;
import static com.fusionx.common.PreferenceKeys.ServerPassword;
import static com.fusionx.common.PreferenceKeys.ServerUserName;
import static com.fusionx.common.PreferenceKeys.Title;
import static com.fusionx.common.PreferenceKeys.URL;

public class BaseServerSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener {

    // Server login
    private EditTextPreference mServerPassword = null;

    // NickServ
    private EditTextPreference mNickServPassword = null;

    // View which notifies user that some fields must be complete
    private MustBeCompleteView mCompleteView = null;

    private final List<EditTextPreference> mEditTexts = new ArrayList<>();

    private ServerSettingsCallbacks mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (ServerSettingsCallbacks) activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final Bundle bundle = getActivity().getIntent().getExtras();

        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        getPreferenceManager().setSharedPreferencesName(mCallback.getFileName());

        addPreferencesFromResource(R.xml.activty_server_settings_prefs);

        final PreferenceScreen prefSet = getPreferenceScreen();

        final ServerTitleEditTextPreference title = (ServerTitleEditTextPreference)
                prefSet.findPreference(Title);
        title.setOnPreferenceChangeListener(this);
        title.setListOfExistingServers(bundle.getStringArrayList("list"));
        mEditTexts.add(title);

        // URL of server
        mCompleteView = (MustBeCompleteView) prefSet.findPreference("must_be_complete");

        // URL of server
        final EditTextPreference url = (EditTextPreference) prefSet.findPreference(URL);
        url.setOnPreferenceChangeListener(this);
        mEditTexts.add(url);

        // Port of server
        final EditTextPreference port = (EditTextPreference) prefSet.findPreference(Port);
        port.setOnPreferenceChangeListener(this);
        port.setSummary(port.getText());

        // Nick of User
        final EditTextPreference realname = (EditTextPreference)
                prefSet.findPreference(RealName);
        realname.setOnPreferenceChangeListener(this);
        realname.setSummary(realname.getText());

        final EditTextPreference severUsername = (EditTextPreference) prefSet.findPreference
                (ServerUserName);
        severUsername.setOnPreferenceChangeListener(this);
        severUsername.setSummary(severUsername.getText());

        mServerPassword = (EditTextPreference) prefSet.findPreference(ServerPassword);
        mServerPassword.setOnPreferenceChangeListener(this);
        updatePasswordSummary(mServerPassword);

        mNickServPassword = (EditTextPreference) prefSet.findPreference(NickServPassword);
        mNickServPassword.setOnPreferenceChangeListener(this);
        updatePasswordSummary(mNickServPassword);

        final Preference autoJoin = prefSet.findPreference("pref_autojoin_intent");
        autoJoin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mCallback.openAutoJoinList();
                return true;
            }
        });

        if (mCallback.canSaveChanges()) {
            for (EditTextPreference edit : mEditTexts) {
                edit.setSummary(edit.getText());
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mCallback.canSaveChanges()) {
            getPreferenceScreen().removePreference(mCompleteView);
        } else {
            mCompleteView.setInitialText(mEditTexts.get(0).getTitle().toString());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue instanceof String) {
            final String newString = (String) newValue;
            final EditTextPreference editTextPreference = (EditTextPreference) preference;
            editTextPreference.setText(newString);
            if (preference != mNickServPassword && preference != mServerPassword) {
                preference.setSummary(newString);
            } else {
                updatePasswordSummary(editTextPreference);
            }
        }
        if (!mCallback.canSaveChanges()) {
            mCallback.setCanSaveChanges(true);
            for (final EditTextPreference edit : mEditTexts) {
                if (StringUtils.isEmpty(edit.getText())) {
                    mCompleteView.setInitialText(edit.getTitle().toString());
                    mCallback.setCanSaveChanges(false);
                    break;
                }
            }
            if (mCallback.canSaveChanges()) {
                getPreferenceScreen().removePreference(mCompleteView);
            }
        }
        return true;
    }

    private void updatePasswordSummary(final EditTextPreference preference) {
        final Activity activity = getActivity();
        if (activity != null) {
            preference.setSummary(StringUtils.isEmpty(preference.getText())
                    ? activity.getString(R.string.server_settings_no_password)
                    : activity.getString(R.string.server_settings_password_set));
        }
    }
}