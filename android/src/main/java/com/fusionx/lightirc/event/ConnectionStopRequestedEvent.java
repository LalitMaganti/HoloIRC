package com.fusionx.lightirc.event;

import co.fusionx.relay.core.Session;

public class ConnectionStopRequestedEvent {

    public final Session connection;

    public ConnectionStopRequestedEvent(final Session connection) {
        this.connection = connection;
    }
}
