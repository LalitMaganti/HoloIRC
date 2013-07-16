/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.fragments.serversettings.BaseServerSettingsFragment;
import com.fusionx.lightirc.interfaces.ServerSettingsListenerInterface;
import com.fusionx.lightirc.misc.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

public class ServerSettingsActivity extends PreferenceActivity implements ServerSettingsListenerInterface {
    @Setter(AccessLevel.PUBLIC)
    private boolean canExit = true;
    @Getter(AccessLevel.PUBLIC)
    private String fileName = null;

    private boolean newServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getThemeInt(getApplicationContext()));

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            newServer = bundle.getBoolean("new", false);
            fileName = bundle.getString("file");
            canExit = !newServer;

            if (bundle.getBoolean("main")) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new BaseServerSettingsFragment())
                        .commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!canExit && newServer) {
            final AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle(getString(R.string.server_settings_save_question_title))
                    .setMessage(getString(R.string.server_settings_save_question_message))
                    .setNegativeButton(getString(R.string.discard), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final File folder = new File(Utils.getSharedPreferencesPath(getApplicationContext())
                                    + fileName + ".xml");
                            folder.delete();
                            finish();
                        }
                    }).setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do nothing
                }
            });
            build.show();
        } else {
            Toast.makeText(this, getString(R.string.server_settings_changes_saved), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean getNewServer() {
        return newServer;
    }
}