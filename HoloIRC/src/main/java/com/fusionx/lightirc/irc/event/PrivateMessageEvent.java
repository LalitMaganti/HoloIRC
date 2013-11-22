package com.fusionx.lightirc.irc.event;

public class PrivateMessageEvent extends Event {

    public final String nick;

    public PrivateMessageEvent(String nick) {
        this.nick = nick;
    }
}
