package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import co.fusionx.relay.misc.NickStorage;

abstract class AbstractNickPreference extends DialogPreference implements TextWatcher {

    EditText mFirstChoice;

    EditText mSecondChoice;

    EditText mThirdChoice;

    AbstractNickPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setPersistent(true);
        setDialogLayoutResource(R.layout.nick_choices_edit_texts);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        onEditTextChanged();
    }

    protected abstract void retrieveNick();

    protected abstract void persistNick();

    @Override
    protected void showDialog(final Bundle state) {
        super.showDialog(state);

        final String text = mFirstChoice.getText().toString();
        mFirstChoice.getText().clear();
        mFirstChoice.getText().append(text);

        mFirstChoice.removeTextChangedListener(this);
        mFirstChoice.addTextChangedListener(this);
        onEditTextChanged();
    }

    @Override
    protected void onBindDialogView(final View view) {
        super.onBindDialogView(view);

        mFirstChoice = (EditText) view.findViewById(R.id.edit_text_nick_first_choice);
        mSecondChoice = (EditText) view.findViewById(R.id.edit_text_nick_second_choice);
        mThirdChoice = (EditText) view.findViewById(R.id.edit_text_nick_third_choice);

        retrieveNick();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && callChangeListener(getNickStorageFromText())) {
            persistNick();
        }
    }

    String getFirstNickText() {
        return mFirstChoice.getText().toString();
    }

    String getSecondNickText() {
        return mSecondChoice.getText().toString();
    }

    String getThirdNickText() {
        return mThirdChoice.getText().toString();
    }

    void onEditTextChanged() {
        final boolean enable = !getFirstNickText().isEmpty();
        final Dialog dlg = getDialog();
        final AlertDialog alertDlg = (AlertDialog) dlg;
        if (alertDlg != null) {
            final Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(enable);
            mFirstChoice.setError(enable ? null : "Must not be empty");
        }
    }

    private NickStorage getNickStorageFromText() {
        return new NickStorage(getFirstNickText(), getSecondNickText(), getThirdNickText());
    }
}
