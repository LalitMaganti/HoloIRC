package com.fusionx.lightirc.event;

import com.fusionx.lightirc.misc.FragmentType;

import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.core.Session;

public class OnConversationChanged {

    public final Session session;

    public final Conversation conversation;

    public final FragmentType fragmentType;

    public OnConversationChanged(final Session session, final Conversation conversation,
            final FragmentType fragmentType) {
        this.session = session;
        this.conversation = conversation;
        this.fragmentType = fragmentType;
    }
}