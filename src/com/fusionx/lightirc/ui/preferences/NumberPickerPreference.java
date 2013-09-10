package com.fusionx.lightirc.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.fusionx.lightirc.R;

import org.holoeverywhere.preference.DialogPreference;

public class NumberPickerPreference extends DialogPreference {
    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPersistent(false);
        setDialogLayoutResource(R.layout.preference_number_picker);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPersistent(false);
        setDialogLayoutResource(R.layout.preference_number_picker);
    }
}
