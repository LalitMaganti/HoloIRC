package com.fusionx.lightirc.ui.dialogbuilder;

import android.content.Context;

public abstract class IgnoreNickPromptDialogBuilder extends PromptDialogBuilder {
    public IgnoreNickPromptDialogBuilder(Context context, String defaultText) {
        super(context, "Nick to ignore", "Nickname of the user to be ignored", defaultText);
    }
}
