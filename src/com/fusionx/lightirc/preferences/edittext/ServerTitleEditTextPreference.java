package com.fusionx.lightirc.preferences.edittext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import java.util.ArrayList;

import lombok.Setter;

public class ServerTitleEditTextPreference extends NonEmptyEditTextPreference {
    @Setter
    public ArrayList<String> listOfExistingServers;

    public ServerTitleEditTextPreference(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    protected AlertDialog onEditTextChanged() {
        AlertDialog dialog = super.onEditTextChanged();
        if (dialog != null && listOfExistingServers != null) {
            for (String title : listOfExistingServers) {
                if (title.toLowerCase().equals(getEditText().getText().toString().toLowerCase())) {
                    getEditText().setError("Server with the same name already exists.");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    break;
                }
            }
        }
        return dialog;
    }
}
