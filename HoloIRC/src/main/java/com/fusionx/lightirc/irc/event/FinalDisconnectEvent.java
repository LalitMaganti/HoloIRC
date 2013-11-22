package com.fusionx.lightirc.irc.event;

public class FinalDisconnectEvent extends ServerEvent {

    public final boolean disconnectExpected;

    public FinalDisconnectEvent(boolean retrySentByUser, String message) {
        super(message);
        this.disconnectExpected = retrySentByUser;
    }
}
