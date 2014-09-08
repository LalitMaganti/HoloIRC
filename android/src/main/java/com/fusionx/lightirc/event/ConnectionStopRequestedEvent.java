package com.fusionx.lightirc.event;

import co.fusionx.relay.base.IRCSession;

public class ConnectionStopRequestedEvent {

    public final IRCSession connection;

    public ConnectionStopRequestedEvent(final IRCSession connection) {
        this.connection = connection;
    }
}
