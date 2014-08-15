package com.fusionx.lightirc.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_NICK;
import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_SECOND_NICK;
import static com.fusionx.lightirc.misc.PreferenceConstants.PREF_THIRD_NICK;

public class NickPreference extends AbstractNickPreference {

    public NickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNickChoices(final String first, final String second, final String third) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PREF_NICK, first);
        editor.putString(PREF_SECOND_NICK, second);
        editor.putString(PREF_THIRD_NICK, third);
        editor.commit();
    }

    @Override
    protected void retrieveNick() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        mFirstChoice.setText(sharedPreferences.getString(PREF_NICK, "HoloIRCUser"));
        mSecondChoice.setText(sharedPreferences.getString(PREF_SECOND_NICK, ""));
        mThirdChoice.setText(sharedPreferences.getString(PREF_THIRD_NICK, ""));
    }

    @Override
    protected void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PREF_NICK, getFirstNickText());
        editor.putString(PREF_SECOND_NICK, getSecondNickText());
        editor.putString(PREF_THIRD_NICK, getThirdNickText());
        editor.commit();
    }
}