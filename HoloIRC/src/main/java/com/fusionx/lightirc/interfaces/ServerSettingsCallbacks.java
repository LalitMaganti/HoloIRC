package com.fusionx.lightirc.interfaces;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public interface ServerSettingsCallbacks extends Preference.OnPreferenceChangeListener {

    public void setupPreferences(final PreferenceScreen screen, final Activity activity);
}