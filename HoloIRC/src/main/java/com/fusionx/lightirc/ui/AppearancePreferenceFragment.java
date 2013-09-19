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

package com.fusionx.lightirc.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.constants.PreferenceConstants;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppearancePreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_settings_fragment);

        setupThemePreference(getPreferenceScreen(), getActivity());
    }

    public static void setupThemePreference(final PreferenceScreen screen,
                                            final Activity activity) {
        final ListPreference themePreference = (ListPreference) screen.findPreference
                (PreferenceConstants.Theme);
        if (themePreference.getEntry() == null) {
            themePreference.setValue("1");
        }
        themePreference.setOnPreferenceChangeListener(new ThemeChangeListener(activity));
        themePreference.setSummary(themePreference.getEntry());
    }

    static class ThemeChangeListener implements Preference.OnPreferenceChangeListener {
        private final Activity mActivity;
        private final ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName className, final IBinder binder) {
                final IRCService service = ((IRCService.IRCBinder) binder).getService();
                service.disconnectAll();
                mActivity.unbindService(mConnection);
                final Intent intent = mActivity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(mActivity.getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
            }
        };

        ThemeChangeListener(final Activity context) {
            mActivity = context;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            final AlertDialog.Builder build = new AlertDialog.Builder(mActivity);
            build.setMessage(mActivity.getString(R.string.appearance_settings_requires_restart))
                    .setPositiveButton(mActivity.getString(R.string.restart),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final Intent service = new Intent(mActivity,
                                            IRCService.class);
                                    service.putExtra("stop", false);
                                    mActivity.bindService(service, mConnection, 0);
                                }
                            });
            build.show();
            return true;
        }
    }
}