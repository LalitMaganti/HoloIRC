package com.fusionx.lightirc.event;

import co.fusionx.relay.core.Session;

public class SessionStopRequestedEvent {

    public final Session session;

    public SessionStopRequestedEvent(final Session session) {
        this.session = session;
    }
}
