package com.fusionx.lightirc.event;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.Nick;

public class OnChannelMentionEvent {

    public final Channel channel;
    public final Nick user;
    public final String message;

    public OnChannelMentionEvent(final Channel channel, Nick user, String message) {
        this.channel = channel;
        this.user = user;
        this.message = message;
    }
}