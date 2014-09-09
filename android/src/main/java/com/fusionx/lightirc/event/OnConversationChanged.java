package com.fusionx.lightirc.event;

import com.fusionx.lightirc.misc.FragmentType;

import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.core.Session;

public class OnConversationChanged {

    public final Session connection;

    public final Conversation conversation;

    public final FragmentType fragmentType;

    public OnConversationChanged(final Session connection, final Conversation conversation,
            final FragmentType fragmentType) {
        this.connection = connection;
        this.conversation = conversation;
        this.fragmentType = fragmentType;
    }
}