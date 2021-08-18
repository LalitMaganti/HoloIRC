package app.holoirc.event;

import co.fusionx.relay.dcc.chat.DCCChatConversation;

public class OnDCCChatEvent {

    public final DCCChatConversation chatConversation;

    public OnDCCChatEvent(final DCCChatConversation chatConversation) {
        this.chatConversation = chatConversation;
    }
}