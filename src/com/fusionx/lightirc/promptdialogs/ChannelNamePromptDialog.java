package com.fusionx.lightirc.promptdialogs;

import android.content.Context;

public abstract class ChannelNamePromptDialog extends PromptDialog {
    public ChannelNamePromptDialog(final Context context) {
        super(context, "Channel Name", "Including the starting #", "");
    }

    public ChannelNamePromptDialog(final Context context, final String channelName) {
        super(context, "Channel Name", "Including the starting #", channelName);
    }
}
