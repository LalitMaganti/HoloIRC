package com.fusionx.lightirc.util;

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import java.util.List;

import java8.util.stream.IntStreams;

public class PreferenceUtils {

    public static void getPreferenceList(final Preference p, final List<Preference> list) {
        if (p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            final PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            IntStreams.range(0, pCount)
                    .mapToObj(pGroup::getPreference)
                    .forEach(pref -> getPreferenceList(pref, list));
        } else {
            list.add(p);
        }
    }
}