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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.util.MiscUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_settings_fragment);

        setupAppVersionPreference(getPreferenceScreen(), getActivity());
    }

    public static void setupAppVersionPreference(final PreferenceScreen screen,
                                                 final Context context) {
        final Preference appVersionPreference = screen.findPreference(PreferenceConstants
                .AppVersion);
        if (appVersionPreference != null) {
            appVersionPreference.setSummary(MiscUtils.getAppVersion(context));
        }
    }
}