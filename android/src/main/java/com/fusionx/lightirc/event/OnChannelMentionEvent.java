package com.fusionx.lightirc.event;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.IRCSession;

public class OnChannelMentionEvent {

    public final IRCSession connection;

    public final Channel channel;

    public OnChannelMentionEvent(final IRCSession connection,final Channel channel) {
        this.connection = connection;
        this.channel = channel;
    }
}