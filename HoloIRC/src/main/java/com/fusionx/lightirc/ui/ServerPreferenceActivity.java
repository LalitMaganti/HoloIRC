package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.ui.preferences.NickPreference;
import com.fusionx.lightirc.ui.preferences.ServerTitleEditTextPreference;
import com.fusionx.lightirc.ui.preferences.ViewPreference;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.ServerConfiguration;

import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_TITLE;
import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_URL;

public class ServerPreferenceActivity extends PreferenceActivity implements
        ServerSettingsCallbacks,
        Preference.OnPreferenceChangeListener {

    public static final String NEW_SERVER = "new";

    public static final String SERVER = "server";

    private boolean mCanSaveChanges = true;

    private ViewPreference mCompletePreference = null;

    private ServerTitleEditTextPreference mTitle = null;

    private EditTextPreference mUrl = null;

    private PreferenceScreen mScreen;

    private BuilderDatabaseSource mSource;

    private ContentValues mContentValues;

    private static void getPreferenceList(final Preference p, final ArrayList<Preference> list) {
        if (p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            final PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            for (int i = 0; i < pCount; i++) {
                getPreferenceList(pGroup.getPreference(i), list);
            }
        } else {
            list.add(p);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());
        super.onCreate(savedInstanceState);

        boolean newServer = getIntent().getBooleanExtra(NEW_SERVER, false);
        mCanSaveChanges = !newServer;

        mSource = new BuilderDatabaseSource(this);
        mSource.open();

        final ServerConfiguration.Builder builder;
        if (newServer) {
            builder = SharedPreferencesUtils.getDefaultNewServer(this);
            mSource.addServer(builder);
        } else {
            builder = getIntent().getParcelableExtra(SERVER);
        }
        mContentValues = mSource.getContentValuesFromBuilder(builder, true);

        if (UIUtils.hasHoneycomb()) {
            final ServerPreferenceFragment fragment = new ServerPreferenceFragment();
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    fragment).commit();
        } else {
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
            getPreferenceManager().setSharedPreferencesName("temp");

            addPreferencesFromResource(R.xml.activty_server_settings_prefs);
            setupPreferences(getPreferenceScreen(), this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath(this) +
                "temp.xml");
        if (folder.exists()) {
            folder.delete();
        }

        if (!mCanSaveChanges) {
            mSource.removeServer(mContentValues);
            Toast.makeText(this, getString(R.string.server_settings_changes_discarded),
                    Toast.LENGTH_SHORT).show();
        } else {
            mSource.updateServer(mContentValues);
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
        }

        mSource.close();
    }

    @Override
    public void setupPreferences(final PreferenceScreen screen, final Activity activity) {
        mScreen = screen;

        mTitle = (ServerTitleEditTextPreference) screen.findPreference(PREF_TITLE);
        mTitle.setListOfExistingServers(activity.getIntent().getStringArrayListExtra("list"));

        mCompletePreference = (ViewPreference) screen.findPreference("must_be_complete");

        // URL of server
        mUrl = (EditTextPreference) screen.findPreference(PREF_URL);

        final Preference preference = screen.findPreference("pref_autojoin_intent");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final Intent intent = new Intent(ServerPreferenceActivity.this,
                        ChannelListActivity.class);
                startActivity(intent);
                return false;
            }
        });

        setPreferenceOnChangeListeners(screen);

        if (!mCanSaveChanges) {
            mCompletePreference.setInitialText(mTitle.getTitle().toString());
        } else {
            screen.removePreference(mCompletePreference);
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (!mCanSaveChanges) {
            if (preference == mTitle && StringUtils.isEmpty(mUrl.getText())) {
                mCompletePreference.setInitialText(mUrl.getTitle());
                mCanSaveChanges = false;
            } else if (preference == mUrl && StringUtils.isEmpty(mTitle.getText())) {
                mCompletePreference.setInitialText(mTitle.getTitle());
                mCanSaveChanges = false;
            } else {
                mScreen.removePreference(mCompletePreference);
                mCanSaveChanges = true;
            }
        }
        return true;
    }

    private void setPreferenceOnChangeListeners(final PreferenceScreen preferenceScreen) {
        final ArrayList<Preference> list = new ArrayList<>();
        getPreferenceList(preferenceScreen, list);

        for (final Preference p : list) {
            final Preference.OnPreferenceChangeListener listener;

            if (p instanceof EditTextPreference) {
                final EditTextPreference editTextPreference = (EditTextPreference) p;
                final String text = mContentValues.getAsString(p.getKey());
                editTextPreference.setText(text);

                listener = new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (preference == mUrl || preference == mTitle) {
                            ServerPreferenceActivity.this.onPreferenceChange(preference, newValue);
                        }
                        mContentValues.put(preference.getKey(), (String) newValue);
                        return true;
                    }
                };
            } else if (p instanceof CheckBoxPreference) {
                final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) p;
                final boolean bool = mContentValues.getAsBoolean(checkBoxPreference.getKey());
                checkBoxPreference.setChecked(bool);

                listener = new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        mContentValues.put(preference.getKey(), (boolean) newValue);
                        return true;
                    }
                };
            } else if (p instanceof NickPreference) {
                listener = new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ((NickPreference) preference).commitToContentValues(mContentValues);
                        return true;
                    }
                };
            } else {
                listener = null;
            }

            if (listener != null) {
                p.setOnPreferenceChangeListener(listener);
            }
        }
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        // TODO - this is a hack - fixit
        return true;
    }
}