package com.fusionx.lightirc.promptdialogs;

import android.content.Context;

public abstract class NickPromptDialog extends PromptDialog {
    public NickPromptDialog(final Context context, final String nick) {
        super(context, "Nick Name", "New nick", nick);
    }
}
