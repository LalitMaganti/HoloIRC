package com.fusionx.lightirc.ui.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import java.util.ArrayList;

public class ServerTitleEditTextPreference extends NonEmptyEditTextPreference {

    private ArrayList<String> mListOfExistingServers;

    public ServerTitleEditTextPreference(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    protected AlertDialog onEditTextChanged() {
        AlertDialog dialog = super.onEditTextChanged();
        if (dialog != null && mListOfExistingServers != null) {
            final String currentText = getEditText().getText().toString();
            if (currentText.contains("/")) {
                getEditText().setError("The character / is not allowed in the title");
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                return dialog;
            }
            for (String title : mListOfExistingServers) {
                if (title.equalsIgnoreCase(currentText)) {
                    getEditText().setError("Server with the same name already exists.");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    return dialog;
                }
            }
        }
        return dialog;
    }

    // Getters and setters
    public void setListOfExistingServers(ArrayList<String> listOfExistingServers) {
        mListOfExistingServers = listOfExistingServers;
    }
}
