package com.fusionx.lightirc.interfaces;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public interface IServerSettings extends Preference.OnPreferenceChangeListener {

    public String getFileName();

    public void setupPreferences(final PreferenceScreen screen, final Activity activity);
}