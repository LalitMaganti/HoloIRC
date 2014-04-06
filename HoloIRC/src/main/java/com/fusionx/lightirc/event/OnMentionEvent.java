package com.fusionx.lightirc.event;

public class OnMentionEvent {

    public final String serverName;

    public final String channelName;

    public OnMentionEvent(String serverName, String channelName) {
        this.serverName = serverName;
        this.channelName = channelName;
    }
}