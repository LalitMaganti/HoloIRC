package com.fusionx.lightirc.event;

import co.fusionx.relay.base.ConnectionStatus;

public class OnCurrentServerStatusChanged {

    public final ConnectionStatus status;

    public OnCurrentServerStatusChanged(ConnectionStatus status) {
        this.status = status;
    }
}