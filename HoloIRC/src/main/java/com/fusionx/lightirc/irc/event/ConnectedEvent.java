package com.fusionx.lightirc.irc.event;

import com.fusionx.lightirc.R;

import android.content.Context;

public class ConnectedEvent extends ServerEvent {

    public ConnectedEvent(final Context context, final String serverUrl) {
        super(String.format(context.getString(R.string.parser_connected), serverUrl));
    }
}