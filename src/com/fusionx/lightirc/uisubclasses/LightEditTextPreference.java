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
    public LightEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        onEditTextChanged();
    }

    void onEditTextChanged() {
        boolean enable = !getEditText().getText().toString().isEmpty();
        Dialog dlg = getDialog();
        if (dlg instanceof AlertDialog) {
            AlertDialog alertDlg = (AlertDialog) dlg;
            Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(enable);
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        getEditText().removeTextChangedListener(this);
        getEditText().addTextChangedListener(this);
        onEditTextChanged();
    }
}
