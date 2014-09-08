package com.fusionx.lightirc.event;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.IRCConnection;

public class OnChannelMentionEvent {

    public final IRCConnection connection;

    public final Channel channel;

    public OnChannelMentionEvent(final IRCConnection connection,final Channel channel) {
        this.connection = connection;
        this.channel = channel;
    }
}