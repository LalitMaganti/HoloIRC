package com.fusionx.lightirc.irc.event;

import com.fusionx.lightirc.R;

import android.content.Context;

public class ConnectedEvent extends ServerEvent {

    private final String serverName;

    public ConnectedEvent(final Context context, final String serverUrl, final String serverName) {
        super(String.format(context.getString(R.string.parser_connected), serverUrl));
        this.serverName = serverName;
    }
}