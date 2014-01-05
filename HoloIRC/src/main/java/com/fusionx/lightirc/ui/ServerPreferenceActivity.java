package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.ui.preferences.NickPreference;
import com.fusionx.lightirc.ui.preferences.ServerTitleEditTextPreference;
import com.fusionx.lightirc.ui.preferences.ViewPreference;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;

import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import java.io.File;

import static com.fusionx.lightirc.constants.PreferenceConstants.PREF_TITLE;
import static com.fusionx.lightirc.constants.PreferenceConstants.PREF_URL;

public class ServerPreferenceActivity extends PreferenceActivity implements
        ServerSettingsCallbacks,
        Preference.OnPreferenceChangeListener {

    private boolean mCanSaveChanges = true;

    private boolean mNewServer = false;

    private String mFileName = null;

    private boolean backPressed = false;

    private ViewPreference mCompletePreference = null;

    private ServerTitleEditTextPreference mTitle = null;

    private EditTextPreference mUrl = null;

    private PreferenceScreen mScreen;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt());

        super.onCreate(savedInstanceState);

        mFileName = getIntent().getStringExtra("file");
        mNewServer = getIntent().getBooleanExtra("new", false);
        mCanSaveChanges = !mNewServer;

        if (UIUtils.hasHoneycomb()) {
            final ServerPreferenceFragment fragment = new ServerPreferenceFragment();
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    fragment).commit();
        } else {
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
            getPreferenceManager().setSharedPreferencesName(mFileName);

            addPreferencesFromResource(R.xml.activty_server_settings_prefs);
            setupPreferences(getPreferenceScreen(), this);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mCanSaveChanges) {
            final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath
                    (this) + "server.xml");
            if (folder.exists()) {
                folder.delete();
            }
            Toast.makeText(this, getString(R.string.server_settings_changes_discarded),
                    Toast.LENGTH_SHORT).show();
            backPressed = true;
        } else if (mNewServer) {
            SharedPreferencesUtils.migrateFileToNewSystem(this, "server.xml");
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
            backPressed = true;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCanSaveChanges || !mNewServer) {
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mCanSaveChanges && !backPressed) {
            final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath
                    (getApplicationContext()) + "server.xml");
            if (folder.exists()) {
                folder.delete();
            }
            Toast.makeText(this, getString(R.string.server_settings_changes_discarded),
                    Toast.LENGTH_SHORT).show();
        } else if (mNewServer && !backPressed) {
            SharedPreferencesUtils.migrateFileToNewSystem(this, "server.xml");
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String getFileName() {
        return mFileName;
    }

    @Override
    public void setupPreferences(final PreferenceScreen screen, final Activity activity) {
        mScreen = screen;

        mTitle = (ServerTitleEditTextPreference) screen.findPreference(PREF_TITLE);
        mTitle.setOnPreferenceChangeListener(this);
        mTitle.setListOfExistingServers(activity.getIntent().getStringArrayListExtra("list"));

        // URL of server
        mCompletePreference = (ViewPreference) screen.findPreference("must_be_complete");

        // URL of server
        mUrl = (EditTextPreference) screen.findPreference(PREF_URL);
        mUrl.setOnPreferenceChangeListener(this);

        Preference preference = screen.findPreference("pref_autojoin_intent");
        assert preference != null;
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final Intent intent = new Intent(ServerPreferenceActivity.this,
                        ChannelListActivity.class);
                intent.putExtra("filename", mFileName);
                startActivity(intent);
                return false;
            }
        });

        if (!mCanSaveChanges) {
            setupNewServer(screen, activity);
            mCompletePreference.setInitialText(mTitle.getTitle().toString());
        } else {
            screen.removePreference(mCompletePreference);
        }
    }

    private void setupNewServer(final PreferenceScreen screen, final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (activity);
        final String firstNick = preferences.getString(PreferenceConstants.PREF_DEFAULT_FIRST_NICK,
                "holoirc");
        final String secondNick = preferences.getString(PreferenceConstants.PREF_DEFAULT_SECOND_NICK, "");
        final String thirdNick = preferences.getString(PreferenceConstants.PREF_DEFAULT_THIRD_NICK, "");

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

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}