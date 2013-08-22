/*
HoloIRC - an IRC client for Android

Copyright 2013 Lalit Maganti

This file is part of HoloIRC.

HoloIRC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HoloIRC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
*/

package com.fusionx.lightirc.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.fusionx.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.fragments.serversetttings.BaseServerSettingsFragment;
import com.fusionx.lightirc.fragments.serversetttings.ListViewSettingsFragment;
import com.fusionx.lightirc.interfaces.ServerSettingsCallbacks;
import com.fusionx.lightirc.misc.SharedPreferencesUtils;

import java.io.File;

public class ServerSettingsActivity extends PreferenceActivity implements ServerSettingsCallbacks {
    private boolean mCanSaveChanges = true;
    private boolean mNewServer = false;
    private String mFileName = null;
    private BaseServerSettingsFragment mBaseFragment;
    private ListViewSettingsFragment mListFragment;
    private boolean mListDisplayed = false;
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeInt(this));

        mBaseFragment = new BaseServerSettingsFragment();
        mListFragment = new ListViewSettingsFragment();

        mFileName = getIntent().getStringExtra("file");
        mNewServer = getIntent().getBooleanExtra("new", false);
        mCanSaveChanges = !mNewServer;

        getFragmentManager().beginTransaction().replace(android.R.id.content, mBaseFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (mListDisplayed) {
            openBaseFragment();
        } else {
            if (!mCanSaveChanges) {
                final File folder = new File(SharedPreferencesUtils.getSharedPreferencesPath
                        (getApplicationContext()) + "server.xml");
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
    public void openAutoJoinList() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(android.R.id.content, mListFragment).commit();
        mListDisplayed = true;
    }

    @Override
    public void openBaseFragment() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(android.R.id.content, mBaseFragment).commit();
        mListDisplayed = false;
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