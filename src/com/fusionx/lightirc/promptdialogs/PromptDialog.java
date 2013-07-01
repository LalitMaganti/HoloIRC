package com.fusionx.lightirc.promptdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public abstract class PromptDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener {
    private final EditText input;

    public PromptDialog(Context context, String title, String hint, String edittextdefaulttext) {
        super(context);

        setTitle(title);

        input = new EditText(context);
        input.setHint(hint);
        input.setText(edittextdefaulttext);
        input.setSingleLine(true);
        setView(input);

        setPositiveButton("OK", this);
        setNegativeButton("Cancel", this);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            onOkClicked(input.getText().toString());
        }
        dialog.dismiss();
    }

    abstract public void onOkClicked(final String input);
}
