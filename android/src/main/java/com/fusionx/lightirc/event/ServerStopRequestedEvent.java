package com.fusionx.lightirc.event;

import co.fusionx.relay.Server;

public class ServerStopRequestedEvent {

    public final Server server;

    public ServerStopRequestedEvent(final Server server) {
        this.server = server;
    }
}
