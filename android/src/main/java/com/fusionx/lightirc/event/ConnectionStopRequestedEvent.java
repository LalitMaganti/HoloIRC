package com.fusionx.lightirc.event;

import co.fusionx.relay.base.IRCConnection;

public class ConnectionStopRequestedEvent {

    public final IRCConnection connection;

    public ConnectionStopRequestedEvent(final IRCConnection connection) {
        this.connection = connection;
    }
}
