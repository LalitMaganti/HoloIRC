package com.fusionx.lightirc.event;

import co.fusionx.relay.dcc.DCCConversation;

public class OnDCCChatEvent {

    public final DCCConversation connection;

    public OnDCCChatEvent(final DCCConversation connection) {
        this.connection = connection;
    }
}