package app.holoirc.event;

import co.fusionx.relay.base.ConnectionStatus;

public class OnCurrentServerStatusChanged {

    public final ConnectionStatus status;

    public OnCurrentServerStatusChanged(ConnectionStatus status) {
        this.status = status;
    }
}