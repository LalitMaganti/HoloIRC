package com.fusionx.lightirc.event;

import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;

public class OnChannelMentionEvent {

    public final Server server;

    public final Channel channel;

    public OnChannelMentionEvent(final Server server, final Channel channel) {
        this.server = server;
        this.channel = channel;
    }
}