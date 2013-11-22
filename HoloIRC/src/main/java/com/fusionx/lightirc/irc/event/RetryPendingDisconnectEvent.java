package com.fusionx.lightirc.irc.event;

public class RetryPendingDisconnectEvent extends ServerEvent {

    public RetryPendingDisconnectEvent(String message) {
        super(message);
    }
}
