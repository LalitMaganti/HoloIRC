package com.fusionx.lightirc.irc.event;

public class PartEvent {
    public final String channelName;

    public PartEvent(String channelName) {
        this.channelName = channelName;
    }
}