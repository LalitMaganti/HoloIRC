package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.R;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

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