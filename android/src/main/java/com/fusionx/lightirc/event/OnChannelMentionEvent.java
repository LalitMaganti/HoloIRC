package com.fusionx.lightirc.event;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.Nick;

public class OnChannelMentionEvent {

    public final Channel channel;
    public final Nick user;
    public final String message;
    public final long timestamp;

    public OnChannelMentionEvent(final Channel channel, Nick user, String message, long timestamp) {
        this.channel = channel;
        this.user = user;
        this.message = message;
        this.timestamp = timestamp;
    }
}