package com.fusionx.lightirc.adapters.actions;

import android.content.Context;

import lombok.Getter;
import lombok.Setter;

public class ServerActionsAdapter extends ActionsArrayAdapter {
    @Getter
    @Setter
    private boolean connected = false;

    public ServerActionsAdapter(final Context context, final String[] objects) {
        super(context, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        return !((position == 0) || (position == 1)) || connected;
    }
}