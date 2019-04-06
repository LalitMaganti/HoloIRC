package app.holoirc.util;

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import java.util.List;

public class PreferenceUtils {

    public static void getPreferenceList(final Preference p, final List<Preference> list) {
        if (p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            final PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            for (int i = 0; i < pCount; i++) {
                getPreferenceList(pGroup.getPreference(i), list);
            }
        } else {
            list.add(p);
        }
    }
}