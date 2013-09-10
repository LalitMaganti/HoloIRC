package com.fusionx.lightirc.ui.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import org.holoeverywhere.app.AlertDialog;

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
                if (title.equalsIgnoreCase(getEditText().getText().toString())) {
                    getEditText().setError("Server with the same name already exists.");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    break;
                }
            }
        }
        return dialog;
    }
}
