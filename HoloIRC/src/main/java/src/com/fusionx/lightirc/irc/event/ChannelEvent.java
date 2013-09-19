package com.fusionx.lightirc.irc.event;

public class ChannelEvent extends Event {
    public final String channelName;
    public final String message;
    public final boolean userListChanged;

    public ChannelEvent(String channelName, String message, boolean userListChanged) {
        this.channelName = channelName;
        this.message = message;
        this.userListChanged = userListChanged;
    }
}