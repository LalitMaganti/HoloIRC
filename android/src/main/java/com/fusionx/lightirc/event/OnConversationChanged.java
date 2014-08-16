package com.fusionx.lightirc.event;

import com.fusionx.lightirc.misc.FragmentType;
import co.fusionx.relay.Conversation;

public class OnConversationChanged {

    public final Conversation conversation;

    public final FragmentType fragmentType;

    public OnConversationChanged(final Conversation conversation, final FragmentType fragmentType) {
        this.conversation = conversation;
        this.fragmentType = fragmentType;
    }
}