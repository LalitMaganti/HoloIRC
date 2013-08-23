package com.fusionx.lightirc.ui.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fusionx.irc.misc.NickStorage;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.PreferenceKeys;

public class NickPreference extends DialogPreference implements TextWatcher {
    private EditText mFirstChoice;
    private EditText mSecondChoice;
    private EditText mThirdChoice;

    public NickPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setPersistent(false);
        setDialogLayoutResource(R.layout.nick_choices_edit_texts);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mFirstChoice = (EditText) view.findViewById(R.id.edit_text_nick_first_choice);
        mSecondChoice = (EditText) view.findViewById(R.id.edit_text_nick_second_choice);
        mThirdChoice = (EditText) view.findViewById(R.id.edit_text_nick_third_choice);

        final SharedPreferences sharedPreferences = getSharedPreferences();
        mFirstChoice.setText(sharedPreferences.getString(PreferenceKeys.FirstNick, "HoloIRCUser"));
        mSecondChoice.setText(sharedPreferences.getString(PreferenceKeys.SecondNick, ""));
        mThirdChoice.setText(sharedPreferences.getString(PreferenceKeys.ThirdNick, ""));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            final SharedPreferences.Editor editor = getEditor();
            editor.putString(PreferenceKeys.FirstNick, mFirstChoice.getText().toString());
            editor.putString(PreferenceKeys.SecondNick, mSecondChoice.getText().toString());
            editor.putString(PreferenceKeys.ThirdNick, mThirdChoice.getText().toString());
            editor.commit();
        }
    }

    void onEditTextChanged() {
        final boolean enable = !mFirstChoice.getText().toString().isEmpty();
        final Dialog dlg = getDialog();
        final AlertDialog alertDlg = (AlertDialog) dlg;
        if (alertDlg != null) {
            final Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(enable);
            mFirstChoice.setError(enable ? null : "Must not be empty");
        }
    }

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
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        onEditTextChanged();
    }

    public NickStorage getNickStorage() {
        return new NickStorage(mFirstChoice.getText().toString(), mSecondChoice.getText().toString(),
                mThirdChoice.getText().toString());
    }
}