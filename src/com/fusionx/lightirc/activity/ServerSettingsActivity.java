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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.fusionx.common.utils.SharedPreferencesUtil;
import com.fusionx.common.utils.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.fragments.serversetttings.BaseServerSettingsFragment;
import com.fusionx.lightirc.fragments.serversetttings.ListViewSettingsFragment;
import com.fusionx.lightirc.misc.ServerSettingsCallbacks;

import java.io.File;

public class ServerSettingsActivity extends FragmentActivity implements ServerSettingsCallbacks {
    private boolean mCanSaveChanges = true;
    private boolean mNewServer = false;
    private String mFileName = null;
    private BaseServerSettingsFragment mBaseFragment;
    private ListViewSettingsFragment mListFragment;
    private boolean mListDisplayed = false;
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getThemeInt(this));

        super.onCreate(savedInstanceState);

        mBaseFragment = new BaseServerSettingsFragment();
        mListFragment = new ListViewSettingsFragment();

        mFileName = getIntent().getStringExtra("file");
        mNewServer = getIntent().getBooleanExtra("new", false);
        mCanSaveChanges = !mNewServer;

        //getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
        //        mBaseFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (mListDisplayed) {
            openBaseFragment();
        } else {
            if (!mCanSaveChanges) {
                final File folder = new File(SharedPreferencesUtil.getSharedPreferencesPath
                        (getApplicationContext()) + "server.xml");
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
    public void openAutoJoinList() {
        //final FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(android.R.id.content, mListFragment).commit();
        mListDisplayed = true;
    }

    @Override
    public void openBaseFragment() {
        //final FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(android.R.id.content, mBaseFragment).commit();
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