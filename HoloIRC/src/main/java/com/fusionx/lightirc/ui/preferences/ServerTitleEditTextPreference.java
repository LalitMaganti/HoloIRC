package com.fusionx.lightirc.ui.preferences;

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
            final String currentText = getEditText().getText().toString();
            if (currentText.contains("/")) {
                getEditText().setError("The character / is not allowed in the title");
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                return dialog;
            }
            for (String title : listOfExistingServers) {
                if (title.equalsIgnoreCase(currentText)) {
                    getEditText().setError("Server with the same name already exists.");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    return dialog;
                }
            }
        }
        return dialog;
    }
}
