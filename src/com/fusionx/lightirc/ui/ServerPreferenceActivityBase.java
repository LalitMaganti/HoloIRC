package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.IServerSettings;
import com.fusionx.lightirc.util.SharedPreferencesUtils;
import com.fusionx.lightirc.util.UIUtils;

import org.holoeverywhere.preference.PreferenceActivity;

import java.io.File;

class ServerPreferenceActivityBase extends PreferenceActivity implements IServerSettings {
    private boolean mCanSaveChanges = true;
    private boolean mNewServer = false;
    private String mFileName = null;
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UIUtils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        mFileName = getIntent().getStringExtra("file");
        mNewServer = getIntent().getBooleanExtra("new", false);
        mCanSaveChanges = !mNewServer;
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
    public boolean canSaveChanges() {
        return mCanSaveChanges;
    }

    @Override
    public void setCanSaveChanges(boolean canSave) {
        mCanSaveChanges = canSave;
    }
}