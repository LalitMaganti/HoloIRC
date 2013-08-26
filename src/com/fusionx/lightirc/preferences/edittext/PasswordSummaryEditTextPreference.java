package com.fusionx.lightirc.preferences.edittext;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.fusionx.lightirc.R;

import org.apache.commons.lang3.StringUtils;

public class PasswordSummaryEditTextPreference extends EditTextPreference {
    public PasswordSummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(final String text) {
        super.setText(text);

        setSummary(StringUtils.isEmpty(text) ? getContext().getString(R.string
                .server_settings_no_password) : getContext().getString(R.string
                .server_settings_password_set));
    }
}