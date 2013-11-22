package com.fusionx.lightirc.irc.event;

public class ErrorEvent extends Event {

    public ErrorEvent(final String errorMessage) {
        super(errorMessage);
    }
}