package com.fusionx.lightirc.promptdialogs;

import android.content.Context;
import com.fusionx.lightirc.R;

public abstract class ChannelNamePromptDialog extends PromptDialog {
    public ChannelNamePromptDialog(final Context context) {
        super(context, context.getString(R.string.prompt_dialog_channel_name),
                context.getString(R.string.prompt_dialog_including_starting), "");
    }

    public ChannelNamePromptDialog(final Context context, final String channelName) {
        super(context, context.getString(R.string.prompt_dialog_channel_name),
                context.getString(R.string.prompt_dialog_including_starting), channelName);
    }
}
