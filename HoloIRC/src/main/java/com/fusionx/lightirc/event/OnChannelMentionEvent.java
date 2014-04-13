package com.fusionx.lightirc.event;

public class OnChannelMentionEvent {

    public final String serverName;

    public final String channelName;

    public OnChannelMentionEvent(String serverName, String channelName) {
        this.serverName = serverName;
        this.channelName = channelName;
    }
}