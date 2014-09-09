package com.fusionx.lightirc.event;

import co.fusionx.relay.core.SessionStatus;

public class OnCurrentServerStatusChanged {

    public final SessionStatus status;

    public OnCurrentServerStatusChanged(SessionStatus status) {
        this.status = status;
    }
}