package com.fusionx.lightirc.irc.event;

public class KickEvent extends Event {

    public final String channelName;

    public KickEvent(String channelName) {
        this.channelName = channelName;
    }
}