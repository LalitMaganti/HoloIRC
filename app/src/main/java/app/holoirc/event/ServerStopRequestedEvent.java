package app.holoirc.event;

import co.fusionx.relay.base.Server;

public class ServerStopRequestedEvent {

    public final Server server;

    public ServerStopRequestedEvent(final Server server) {
        this.server = server;
    }
}
