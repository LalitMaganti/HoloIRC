package com.fusionx.lightirc.adapters.actions;

import android.content.Context;
import com.fusionx.lightirc.R;

import java.util.ArrayList;
import java.util.Arrays;

public class UserChannelActionsAdapter extends ActionsArrayAdapter {
    public UserChannelActionsAdapter(Context context) {
        super(context, context.getResources().getStringArray(R.array.channel_actions));
    }

    public void setServerVisible() {
        mObjects = new ArrayList<>();
    }

    public void setChannelVisible(boolean visible) {
        mObjects = visible ? Arrays.asList(mContext.getResources()
                .getStringArray(R.array.channel_actions)) :
                Arrays.asList(mContext.getResources().getStringArray(R.array.user_actions));
    }
}
