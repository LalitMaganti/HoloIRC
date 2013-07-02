package com.fusionx.lightirc.promptdialogs;

import android.content.Context;
import com.fusionx.lightirc.R;

public abstract class NickPromptDialog extends PromptDialog {
    public NickPromptDialog(final Context context, final String nick) {
        super(context, context.getString(R.string.nickname), context.getString(R.string.new_nick), nick);
    }
}
