package com.fusionx.lightirc.event;

import co.fusionx.relay.dcc.chat.DCCChatConversation;

public class OnDCCChatEvent {

    public final DCCChatConversation connection;

    public OnDCCChatEvent(final DCCChatConversation connection) {
        this.connection = connection;
    }
}