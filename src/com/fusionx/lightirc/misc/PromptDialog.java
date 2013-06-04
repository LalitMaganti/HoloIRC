package com.fusionx.lightirc.misc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

public abstract class PromptDialog extends AlertDialog.Builder implements OnClickListener {
    private final EditText input;

    public PromptDialog(Context context) {
        super(context);
        setTitle("Channel Name");

        input = new EditText(context);
        input.setHint("Channel name (including the starting #");
        setView(input);

        setPositiveButton("OK", this);
        setNegativeButton("Cancel", this);
    }

    public PromptDialog(Context context, String message) {
        super(context);
        setTitle("Channel Name");

        input = new EditText(context);
        input.setText(message);
        setView(input);

        setPositiveButton("OK", this);
        setNegativeButton("Cancel", this);
    }

    void onCancelClicked(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (onOkClicked(input.getText().toString())) {
                dialog.dismiss();
            }
        } else {
            onCancelClicked(dialog);
        }
    }

    abstract public boolean onOkClicked(String input);
}
