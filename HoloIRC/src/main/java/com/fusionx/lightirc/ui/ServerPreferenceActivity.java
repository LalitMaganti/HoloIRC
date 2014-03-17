package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.misc.PreferenceConstants;
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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
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

    private ServerConfiguration.Builder mBuilder;

    private boolean mNewServer;

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

        mBuilder = getIntent().getParcelableExtra(SERVER);
        mNewServer = getIntent().getBooleanExtra(NEW_SERVER, false);
        mCanSaveChanges = !mNewServer;

        mSource = new BuilderDatabaseSource(this);
        mSource.open();

        if (mNewServer) {
            mBuilder = new ServerConfiguration.Builder();
            mSource.addBuilder(mBuilder);
        }

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

        final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath
                (this) + "temp.xml");
        if (folder.exists()) {
            folder.delete();
        }

        if (!mCanSaveChanges) {
            Toast.makeText(this, getString(R.string.server_settings_changes_discarded),
                    Toast.LENGTH_SHORT).show();
        } else {
            mSource.updateBuilder(mContentValues);
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

        // URL of server
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
    }

    private void setupNewServer(final PreferenceScreen screen, final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (activity);
        final String firstNick = preferences.getString(PreferenceConstants.PREF_DEFAULT_FIRST_NICK,
                "holoirc");
        final String secondNick = preferences
                .getString(PreferenceConstants.PREF_DEFAULT_SECOND_NICK, "");
        final String thirdNick = preferences
                .getString(PreferenceConstants.PREF_DEFAULT_THIRD_NICK, "");

        final String realName = preferences.getString(PreferenceConstants.PREF_DEFAULT_REALNAME,
                "HoloIRCUser");
        final boolean autoNick = preferences.getBoolean(PreferenceConstants
                .PREF_DEFAULT_AUTO_NICK, true);

        final NickPreference nickPreference = (NickPreference) screen.findPreference
                ("pref_nick_storage");
        nickPreference.setFirstChoice(firstNick);
        nickPreference.setSecondChoice(secondNick);
        nickPreference.setThirdChoice(thirdNick);

        final EditTextPreference realNamePref = (EditTextPreference) screen
                .findPreference(PreferenceConstants.PREF_REALNAME);
        realNamePref.setText(realName);
        final CheckBoxPreference autoNickPref = (CheckBoxPreference) screen
                .findPreference(PreferenceConstants.PREF_AUTO_NICK);
        autoNickPref.setChecked(autoNick);
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

        mContentValues = mSource.getContentValuesFromBuilder(mBuilder);
        for (final Preference p : list) {
            if (p instanceof EditTextPreference) {
                final EditTextPreference editTextPreference = (EditTextPreference) p;
                final String text = mContentValues.getAsString(p.getKey());
                editTextPreference.setText(text);
            } else if (p instanceof CheckBoxPreference) {
                final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) p;
                final boolean bool = mContentValues.getAsBoolean(checkBoxPreference.getKey());
                checkBoxPreference.setChecked(bool);
            }

            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (preference == mUrl || preference == mTitle) {
                        ServerPreferenceActivity.this.onPreferenceChange(preference, newValue);
                    }
                    if (preference instanceof EditTextPreference) {
                        mContentValues.put(preference.getKey(), (String) newValue);
                    } else if (preference instanceof CheckBoxPreference) {
                        mContentValues.put(preference.getKey(), (boolean) newValue);
                    } else if (preference instanceof NickPreference) {
                        ((NickPreference) preference).commitToContentValues(mContentValues);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        // TODO - this is a hack - fixit
        return true;
    }
}