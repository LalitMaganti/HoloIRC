package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.constants.PreferenceConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

public class DefaultNickPreference extends AbstractNickPreference {

    public DefaultNickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void retrieveNick() {
        final SharedPreferences preferences = getSharedPreferences();
        mFirstChoice.setText(preferences.getString(PreferenceConstants.DefaultFirstNick,
                "HoloIRCUser"));
        mSecondChoice.setText(preferences.getString(PreferenceConstants.DefaultSecondNick, ""));
        mThirdChoice.setText(preferences.getString(PreferenceConstants.DefaultThirdNick, ""));
    }

    @Override
    protected void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.DefaultFirstNick, mFirstChoice.getText().toString());
        editor.putString(PreferenceConstants.DefaultSecondNick, mSecondChoice.getText().toString());
        editor.putString(PreferenceConstants.DefaultThirdNick, mThirdChoice.getText().toString());
        editor.commit();
    }
}
