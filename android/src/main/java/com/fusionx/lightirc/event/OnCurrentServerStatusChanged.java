package com.fusionx.lightirc.event;

import co.fusionx.relay.base.SessionStatus;

public class OnCurrentServerStatusChanged {

    public final SessionStatus status;

    public OnCurrentServerStatusChanged(SessionStatus status) {
        this.status = status;
    }
}