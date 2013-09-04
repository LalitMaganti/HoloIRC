package com.fusionx.lightirc.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import com.fusionx.lightirc.misc.PreferenceKeys;

public class NickPreference extends AbstractNickPreference {
    public NickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // TODO - make parent return SharedPreference
    @Override
    protected void retrieveNick() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        mFirstChoice.setText(sharedPreferences.getString(PreferenceKeys.FirstNick, "HoloIRCUser"));
        mSecondChoice.setText(sharedPreferences.getString(PreferenceKeys.SecondNick, ""));
        mThirdChoice.setText(sharedPreferences.getString(PreferenceKeys.ThirdNick, ""));
    }

    // TODO - make parent return Editor
    @Override
    protected void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceKeys.FirstNick, mFirstChoice.getText().toString());
        editor.putString(PreferenceKeys.SecondNick, mSecondChoice.getText().toString());
        editor.putString(PreferenceKeys.ThirdNick, mThirdChoice.getText().toString());
        editor.commit();
    }

    public void setFirstChoice(final String first) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceKeys.FirstNick, first);
        editor.commit();
    }

    public void setSecondChoice(final String second) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceKeys.SecondNick, second);
        editor.commit();
    }

    public void setThirdChoice(final String third) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceKeys.ThirdNick, third);
        editor.commit();
    }
}