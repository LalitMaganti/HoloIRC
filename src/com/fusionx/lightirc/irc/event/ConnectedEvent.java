package com.fusionx.lightirc.irc.event;

import android.content.Context;

import com.fusionx.lightirc.R;

public class ConnectedEvent extends ServerEvent {
    private final String serverName;

    public ConnectedEvent(final Context context, final String serverUrl, final String serverName) {
        super(String.format(context.getString(R.string.parser_connected), serverUrl));
        this.serverName = serverName;
    }
}