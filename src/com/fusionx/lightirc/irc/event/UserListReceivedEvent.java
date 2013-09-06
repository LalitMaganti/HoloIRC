package com.fusionx.lightirc.irc.event;

public class UserListReceivedEvent extends ChannelEvent {
    public UserListReceivedEvent(String channelName) {
        super(channelName, "", false);
    }
}