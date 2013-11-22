package com.fusionx.lightirc.irc.event;

public class ServerEvent extends Event {

    public final String message;

    public ServerEvent(final String message) {
        this.message = message;
    }
}