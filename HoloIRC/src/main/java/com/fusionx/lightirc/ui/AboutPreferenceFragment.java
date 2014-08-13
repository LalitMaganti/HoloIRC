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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceConstants;
import com.fusionx.lightirc.util.MiscUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class AboutPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_settings_fragment);

        final PreferenceScreen screen = getPreferenceScreen();
        final Context context = getActivity();
        final Preference appVersionPreference = screen.findPreference(PreferenceConstants
                .PREF_APP_VERSION);

        if (appVersionPreference != null) {
            appVersionPreference.setSummary(MiscUtils.getAppVersion(context));
        }

        final Preference source = screen.findPreference(PreferenceConstants.PREF_SOURCE);
        if (source != null) {
            source.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent browserIntent = new Intent("android.intent.action.VIEW",
                                    Uri.parse("http://github.com/tilal6991/HoloIRC"));
                            context.startActivity(browserIntent);
                            return true;
                        }
                    }
            );
        }
    }
}