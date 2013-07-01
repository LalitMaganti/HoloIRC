package com.fusionx.lightirc.uisubclasses;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

public abstract class ChannelNamePromptDialog extends AlertDialog.Builder implements OnClickListener {
    private final EditText input;

    public ChannelNamePromptDialog(final Context context) {
        super(context);
        setTitle("Channel Name");

        input = new EditText(context);
        input.setHint("Channel name (including the starting #");
        setView(input);

        setPositiveButton("OK", this);
        setNegativeButton("Cancel", this);
    }

    public ChannelNamePromptDialog(final Context context, final String message) {
        super(context);
        setTitle("Channel Name");

        input = new EditText(context);
        input.setText(message);
        setView(input);

        setPositiveButton("OK", this);
        setNegativeButton("Cancel", this);
    }

    void onCancelClicked(final DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            onOkClicked(dialog, input.getText().toString());
            //dialog.dismiss();
        } else {
            onCancelClicked(dialog);
        }
    }

    abstract public void onOkClicked(DialogInterface dialog, String input);
}
