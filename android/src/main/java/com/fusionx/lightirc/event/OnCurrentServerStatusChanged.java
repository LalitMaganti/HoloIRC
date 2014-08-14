package com.fusionx.lightirc.event;

import com.fusionx.relay.ConnectionStatus;

public class OnCurrentServerStatusChanged {

    public final ConnectionStatus status;

    public OnCurrentServerStatusChanged(ConnectionStatus status) {
        this.status = status;
    }
}