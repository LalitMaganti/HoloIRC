package com.fusionx.lightirc.event;

import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.interfaces.Conversation;

public class OnConversationChanged {

    public final Conversation conversation;

    public final FragmentType fragmentType;

    public OnConversationChanged(final Conversation conversation, FragmentType fragmentType) {
        this.conversation = conversation;
        this.fragmentType = fragmentType;
    }
}