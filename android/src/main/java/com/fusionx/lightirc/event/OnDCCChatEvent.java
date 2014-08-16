package com.fusionx.lightirc.event;

import co.fusionx.relay.dcc.connection.DCCChatConnection;

public class OnDCCChatEvent {

    public final DCCChatConnection connection;

    public OnDCCChatEvent(final DCCChatConnection connection) {
        this.connection = connection;
    }
}