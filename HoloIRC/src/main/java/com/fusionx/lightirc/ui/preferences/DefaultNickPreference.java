package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.misc.PreferenceConstants;

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
        mFirstChoice.setText(preferences.getString(PreferenceConstants.PREF_DEFAULT_FIRST_NICK,
                "HoloIRCUser"));
        mSecondChoice
                .setText(preferences.getString(PreferenceConstants.PREF_DEFAULT_SECOND_NICK, ""));
        mThirdChoice
                .setText(preferences.getString(PreferenceConstants.PREF_DEFAULT_THIRD_NICK, ""));
    }

    @Override
    protected void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.PREF_DEFAULT_FIRST_NICK,
                mFirstChoice.getText().toString());
        editor.putString(PreferenceConstants.PREF_DEFAULT_SECOND_NICK,
                mSecondChoice.getText().toString());
        editor.putString(PreferenceConstants.PREF_DEFAULT_THIRD_NICK,
                mThirdChoice.getText().toString());
        editor.commit();
    }
}
