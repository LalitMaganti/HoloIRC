package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.model.db.BuilderDatabaseSource;
import com.fusionx.lightirc.ui.preferences.NickPreference;
import com.fusionx.lightirc.ui.preferences.ServerTitleEditTextPreference;
import com.fusionx.lightirc.ui.preferences.ViewPreference;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import java.io.File;
import java.util.ArrayList;

import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_TITLE;
import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_URL;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_IGNORE_LIST;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_ONE;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_THREE;
import static com.fusionx.lightirc.model.db.DatabaseContract.ServerTable.COLUMN_NICK_TWO;

public class ServerPreferenceActivity extends PreferenceActivity implements
        ServerSettingsCallbacks {

    public static final String NEW_SERVER = "new";

    public static final String SERVER = "server";

    private static final int CHANNEL_LIST = 10;

    private boolean mCanSaveChanges = true;

    private ViewPreference mCompletePreference = null;

    private ServerTitleEditTextPreference mTitle = null;

    private EditTextPreference mUrl = null;

    private PreferenceScreen mScreen;

    private BuilderDatabaseSource mSource;

    private ContentValues mContentValues;

    private boolean mNewServer;

    private ServerConfiguration.Builder mBuilder;

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
                intent.putExtra("contentValues", mContentValues);
                startActivityForResult(intent, CHANNEL_LIST);
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
    public boolean onPreferenceChange(final Preference preference) {
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
                setResult(RESULT_OK);
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());
        super.onCreate(savedInstanceState);

        mNewServer = getIntent().getBooleanExtra(NEW_SERVER, false);
        mCanSaveChanges = !mNewServer;

        mSource = new BuilderDatabaseSource(this);
        mSource.open();

        if (mNewServer) {
            mBuilder = SharedPreferencesUtils.getDefaultNewServer(this);
            setResult(RESULT_CANCELED);
        } else {
            mBuilder = getIntent().getParcelableExtra(SERVER);
            setResult(RESULT_OK);
        }
        mContentValues = mSource.getContentValuesFromBuilder(mBuilder, !mNewServer);
        // If it's a new server, we can't allow ignore list to be null - just put an empty string
        // in for now - TODO - fix this
        if (mNewServer) {
            mContentValues.put(COLUMN_IGNORE_LIST, "");
        }

        final ServerPreferenceFragment fragment = new ServerPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                fragment).commit();
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        // TODO - this is a hack - fixit
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath(this) +
                "tempUselessFile.xml");
        if (folder.exists()) {
            folder.delete();
        }
        mSource.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CHANNEL_LIST) {
                mContentValues = data.getParcelableExtra("contentValues");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // In onPause we added the new server if it was new so don't add it again
        if (mCanSaveChanges) {
            mNewServer = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCanSaveChanges) {
            if (mNewServer) {
                mSource.addServer(mContentValues);
            } else {
                mSource.updateServer(mContentValues);
            }
        }
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
                            ServerPreferenceActivity.this.onPreferenceChange(preference);
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
                final NickPreference nickPreference = (NickPreference) p;
                nickPreference.setNickChoices(mContentValues.getAsString(COLUMN_NICK_ONE),
                        mContentValues.getAsString(COLUMN_NICK_TWO),
                        mContentValues.getAsString(COLUMN_NICK_THREE));

                listener = new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        final NickStorage storage = (NickStorage) newValue;
                        mContentValues.put(COLUMN_NICK_ONE, storage.getFirstChoiceNick());
                        mContentValues.put(COLUMN_NICK_TWO, storage.getSecondChoiceNick());
                        mContentValues.put(COLUMN_NICK_THREE, storage.getThirdChoiceNick());
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
}