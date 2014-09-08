package com.fusionx.lightirc.event;

import com.fusionx.lightirc.misc.FragmentType;

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.IRCConnection;

public class OnConversationChanged {

    public final IRCConnection connection;

    public final Conversation conversation;

    public final FragmentType fragmentType;

    public OnConversationChanged(final IRCConnection connection, final Conversation conversation,
            final FragmentType fragmentType) {
        this.connection = connection;
        this.conversation = conversation;
        this.fragmentType = fragmentType;
    }
}