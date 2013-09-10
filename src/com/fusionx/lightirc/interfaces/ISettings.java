package com.fusionx.lightirc.interfaces;

import org.holoeverywhere.preference.PreferenceScreen;

public interface ISettings {
    void setupNumberPicker(final PreferenceScreen screen);

    void setupThemePreference(final PreferenceScreen screen);

    void setupAppVersionPreference(final PreferenceScreen screen);
}