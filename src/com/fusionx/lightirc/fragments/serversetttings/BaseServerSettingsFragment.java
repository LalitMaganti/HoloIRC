package com.fusionx.lightirc.fragments.serversetttings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.preferences.edittext.ServerTitleEditTextPreference;
import com.fusionx.lightirc.views.MustBeCompleteView;

import org.apache.commons.lang3.StringUtils;

import static com.fusionx.common.PreferenceKeys.Title;
import static com.fusionx.common.PreferenceKeys.URL;

public class BaseServerSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener {

    private ServerTitleEditTextPreference mTitle;
    private EditTextPreference mUrl;

    // View which notifies user that some fields must be complete
    private MustBeCompleteView mCompleteView = null;

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

        mTitle = (ServerTitleEditTextPreference)
                findPreference(Title);
        mTitle.setOnPreferenceChangeListener(this);
        mTitle.setListOfExistingServers(bundle.getStringArrayList("list"));

        // URL of server
        mCompleteView = (MustBeCompleteView) findPreference("must_be_complete");

        // URL of server
        mUrl = (EditTextPreference) findPreference(URL);
        mUrl.setOnPreferenceChangeListener(this);

        final Preference autoJoin = findPreference("pref_autojoin_intent");
        if (autoJoin != null) {
            autoJoin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mCallback.openAutoJoinList();
                    return true;
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mCallback.canSaveChanges()) {
            getPreferenceScreen().removePreference(mCompleteView);
        } else {
            mCompleteView.setInitialText(mTitle.getTitle().toString());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCallback.canSaveChanges()) {
            if (StringUtils.isEmpty(mTitle.getText())) {
                mCompleteView.setInitialText(mTitle.getTitle().toString());
                mCallback.setCanSaveChanges(false);
            } else if (StringUtils.isEmpty(mTitle.getText())) {
                mCompleteView.setInitialText(mUrl.getTitle().toString());
                mCallback.setCanSaveChanges(false);
            } else {
                mCallback.setCanSaveChanges(true);
                getPreferenceScreen().removePreference(mCompleteView);
            }
        }
        return true;
    }
}