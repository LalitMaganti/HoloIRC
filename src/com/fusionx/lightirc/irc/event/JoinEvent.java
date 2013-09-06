package com.fusionx.lightirc.irc.event;

public class JoinEvent {
    public final String channelToJoin;

    public JoinEvent(final String channelToJoin) {
        this.channelToJoin = channelToJoin;
    }
}