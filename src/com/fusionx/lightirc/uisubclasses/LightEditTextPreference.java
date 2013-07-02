/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

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
