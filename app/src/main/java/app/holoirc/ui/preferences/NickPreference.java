package app.holoirc.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import app.holoirc.misc.PreferenceConstants;

public class NickPreference extends AbstractNickPreference {

    public NickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNickChoices(final String first, final String second, final String third) {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.PREF_NICK, first);
        editor.putString(PreferenceConstants.PREF_SECOND_NICK, second);
        editor.putString(PreferenceConstants.PREF_THIRD_NICK, third);
        editor.commit();
    }

    @Override
    protected void retrieveNick() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        mFirstChoice.setText(sharedPreferences.getString(PreferenceConstants.PREF_NICK, "HoloIRCUser"));
        mSecondChoice.setText(sharedPreferences.getString(PreferenceConstants.PREF_SECOND_NICK, ""));
        mThirdChoice.setText(sharedPreferences.getString(PreferenceConstants.PREF_THIRD_NICK, ""));
    }

    @Override
    protected void persistNick() {
        final SharedPreferences.Editor editor = getEditor();
        editor.putString(PreferenceConstants.PREF_NICK, getFirstNickText());
        editor.putString(PreferenceConstants.PREF_SECOND_NICK, getSecondNickText());
        editor.putString(PreferenceConstants.PREF_THIRD_NICK, getThirdNickText());
        editor.commit();
    }
}