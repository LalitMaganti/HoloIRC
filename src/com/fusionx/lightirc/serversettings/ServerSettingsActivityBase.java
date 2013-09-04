package com.fusionx.lightirc.serversettings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.fusionx.common.utils.SharedPreferencesUtil;
import com.fusionx.common.utils.Utils;
import com.fusionx.lightirc.R;

import java.io.File;

class ServerSettingsActivityBase extends PreferenceActivity implements IServerSettings {
    protected boolean mCanSaveChanges = true;
    protected boolean mNewServer = false;
    protected String mFileName = null;
    protected boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(this));

        super.onCreate(savedInstanceState);

            mFileName = getIntent().getStringExtra("file");
            mNewServer = getIntent().getBooleanExtra("new", false);
            mCanSaveChanges = !mNewServer;
    }

    @Override
    public void onBackPressed() {
        if (!mCanSaveChanges) {
            final File folder = new File(SharedPreferencesUtil.getSharedPreferencesPath
                    (this) + "server.xml");
            if (folder.exists()) {
                folder.delete();
            }
            Toast.makeText(this, getString(R.string.server_settings_changes_discarded),
                    Toast.LENGTH_SHORT).show();
            backPressed = true;
        } else if (mNewServer) {
            SharedPreferencesUtil.migrateFileToNewSystem(this, "server.xml");
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
            backPressed = true;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mCanSaveChanges && !backPressed) {
            final File folder = new File(SharedPreferencesUtil.getSharedPreferencesPath
                    (getApplicationContext()) + "server.xml");
            if (folder.exists()) {
                folder.delete();
            }
            Toast.makeText(this, getString(R.string.server_settings_changes_discarded),
                    Toast.LENGTH_SHORT).show();
        } else if (mNewServer && !backPressed) {
            SharedPreferencesUtil.migrateFileToNewSystem(this, "server.xml");
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.server_settings_changes_saved),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String getFileName() {
        return mFileName;
    }

    @Override
    public boolean canSaveChanges() {
        return mCanSaveChanges;
    }

    @Override
    public void setCanSaveChanges(boolean canSave) {
        mCanSaveChanges = canSave;
    }
}