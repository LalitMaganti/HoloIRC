package com.fusionx.lightirc.irc.event;

public class UserListReceivedEvent {
    public final String channelName;

    public UserListReceivedEvent(String channelName) {
        this.channelName = channelName;
    }
}