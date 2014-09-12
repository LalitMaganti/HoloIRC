package com.fusionx.lightirc.event;

import co.fusionx.relay.dcc.core.DCCChatConversation;

public class OnDCCChatEvent {

    public final DCCChatConversation chatConversation;

    public OnDCCChatEvent(final DCCChatConversation chatConversation) {
        this.chatConversation = chatConversation;
    }
}