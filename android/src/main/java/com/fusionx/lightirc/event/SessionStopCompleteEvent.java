package com.fusionx.lightirc.event;

import co.fusionx.relay.core.Session;

public class SessionStopCompleteEvent {

    public final Session session;

    public SessionStopCompleteEvent(final Session session) {
        this.session = session;
    }
}
