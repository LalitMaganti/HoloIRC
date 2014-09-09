package com.fusionx.lightirc.event;

import co.fusionx.relay.conversation.Channel;
import co.fusionx.relay.core.Session;

public class OnChannelMentionEvent {

    public final Session connection;

    public final Channel channel;

    public OnChannelMentionEvent(final Session connection,final Channel channel) {
        this.connection = connection;
        this.channel = channel;
    }
}