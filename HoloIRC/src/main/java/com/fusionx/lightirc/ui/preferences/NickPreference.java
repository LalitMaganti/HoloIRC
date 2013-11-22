package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.constants.PreferenceConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

public class NickPreference extends AbstractNickPreference {

    public NickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // TODO - make parent return SharedPreference
    @Override
    protected void retrieveNick() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        mFirstChoice.setText(sharedPreferences.getString(PreferenceConstants.FirstNick,
                "HoloIRCUser"));
        mSecondChoice.setText(sharedPreferences.getString(PreferenceConstants.SecondNick, ""));
        mThirdChoice.setText(sharedPreferences.getString(PreferenceConstants.ThirdNick, ""));
    }

    // TODO - make parent return Editor
    @Override
    protected void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.FirstNick, mFirstChoice.getText().toString());
        editor.putString(PreferenceConstants.SecondNick, mSecondChoice.getText().toString());
        editor.putString(PreferenceConstants.ThirdNick, mThirdChoice.getText().toString());
        editor.commit();
    }

    public void setFirstChoice(final String first) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.FirstNick, first);
        editor.commit();
    }

    public void setSecondChoice(final String second) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.SecondNick, second);
        editor.commit();
    }

    public void setThirdChoice(final String third) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.ThirdNick, third);
        editor.commit();
    }
}