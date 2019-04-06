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

package app.holoirc.ui.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * A EditTextPreference sub class which disables the positive button when the EditText is empty
 *
 * @author Lalit Maganti
 */
public class NonEmptyEditTextPreference extends SummaryEditTextPreference implements TextWatcher {

    public NonEmptyEditTextPreference(final Context context, final AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int before,
            final int count) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before,
            final int count) {
    }

    @Override
    public void afterTextChanged(final Editable s) {
        onEditTextChanged();
    }

    @Override
    protected void showDialog(final Bundle state) {
        super.showDialog(state);

        final String text = getEditText().getText().toString();
        getEditText().getText().clear();
        getEditText().getText().append(text);

        getEditText().removeTextChangedListener(this);
        getEditText().addTextChangedListener(this);
        onEditTextChanged();
    }

    AlertDialog onEditTextChanged() {
        final boolean enable = !getEditText().getText().toString().isEmpty();
        final Dialog dlg = getDialog();
        final AlertDialog alertDlg = (AlertDialog) dlg;
        if (alertDlg != null) {
            final Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(enable);
            getEditText().setError(enable ? null : "Must not be empty");
        }
        return alertDlg;
    }
}