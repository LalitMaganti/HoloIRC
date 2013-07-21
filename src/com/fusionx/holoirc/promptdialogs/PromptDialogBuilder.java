/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.holoirc.promptdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import com.fusionx.holoirc.R;

public abstract class PromptDialogBuilder extends AlertDialog.Builder
        implements DialogInterface.OnClickListener, TextWatcher {
    private final EditText input;
    private AlertDialog dialog = null;

    PromptDialogBuilder(final Context context, final String title, final String hint, final String defaultText) {
        super(context);

        setTitle(title);

        input = new EditText(context);
        input.setHint(hint);
        input.setText(defaultText);
        input.getText().clear();
        input.append(defaultText);
        input.setSingleLine(true);
        setView(input);

        setPositiveButton(context.getString(R.string.ok), this);
        setNegativeButton(context.getString(R.string.cancel), this);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            onOkClicked(input.getText().toString());
        }
        dialog.dismiss();
    }

    @Override
    public AlertDialog show() {
        dialog = super.show();
        updateButton();

        input.removeTextChangedListener(this);
        input.addTextChangedListener(this);

        return dialog;
    }

    abstract public void onOkClicked(final String input);

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        updateButton();
    }

    private void updateButton() {
        final boolean enable = !input.getText().toString().isEmpty();
        final Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btn.setEnabled(enable);
    }
}