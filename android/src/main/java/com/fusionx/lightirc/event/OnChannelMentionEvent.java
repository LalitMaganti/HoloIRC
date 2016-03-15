package com.fusionx.lightirc.event;

import co.fusionx.relay.Channel;

public class OnChannelMentionEvent {

    public final Channel channel;

    public OnChannelMentionEvent(final Channel channel) {
        this.channel = channel;
    }
}