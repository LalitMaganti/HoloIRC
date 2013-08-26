package com.fusionx.lightirc.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import com.fusionx.common.PreferenceKeys;

public class DefaultNickPreference extends AbstractNickPreference {
    public DefaultNickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void retrieveNick() {
        final SharedPreferences preferences = getSharedPreferences();
        mFirstChoice.setText(preferences.getString(PreferenceKeys.DefaultFirstNick,
                "HoloIRCUser"));
        mSecondChoice.setText(preferences.getString(PreferenceKeys.DefaultSecondNick, ""));
        mThirdChoice.setText(preferences.getString(PreferenceKeys.DefaultThirdNick, ""));
    }

    @Override
    public void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceKeys.DefaultFirstNick, mFirstChoice.getText().toString());
        editor.putString(PreferenceKeys.DefaultSecondNick, mSecondChoice.getText().toString());
        editor.putString(PreferenceKeys.DefaultThirdNick, mThirdChoice.getText().toString());
        editor.commit();
    }
}
