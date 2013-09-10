package com.fusionx.lightirc.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;

import org.holoeverywhere.preference.EditTextPreference;

public class SummaryEditTextPreference extends EditTextPreference {
    public SummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(final String text) {
        super.setText(text);

        setSummary(text);
    }
}