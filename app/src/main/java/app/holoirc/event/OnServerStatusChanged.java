package app.holoirc.event;

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Server;

public class OnServerStatusChanged {
    public final Server server;
    public final ConnectionStatus status;

    public OnServerStatusChanged(Server server, ConnectionStatus status) {
        this.server = server;
        this.status = status;
    }
}
