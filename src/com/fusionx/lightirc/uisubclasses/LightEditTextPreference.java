package com.fusionx.lightirc.uisubclasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;

public class LightEditTextPreference extends EditTextPreference implements TextWatcher {
    public LightEditTextPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int before, final int count) {
    }

    @Override
    public void afterTextChanged(final Editable s) {
        onEditTextChanged();
    }

    void onEditTextChanged() {
        final boolean enable = !getEditText().getText().toString().isEmpty();
        final Dialog dlg = getDialog();
        if (dlg instanceof AlertDialog) {
            final AlertDialog alertDlg = (AlertDialog) dlg;
            final Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(enable);
        }
    }

    @Override
    protected void showDialog(final Bundle state) {
        super.showDialog(state);

        getEditText().removeTextChangedListener(this);
        getEditText().addTextChangedListener(this);
        onEditTextChanged();
    }
}
