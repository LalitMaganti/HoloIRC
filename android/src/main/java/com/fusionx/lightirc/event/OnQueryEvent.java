package com.fusionx.lightirc.event;

import co.fusionx.relay.core.Session;
import co.fusionx.relay.conversation.QueryUser;

public class OnQueryEvent {

    public final Session connection;

    public final QueryUser queryUser;

    public OnQueryEvent(final Session connection, final QueryUser queryUser) {
        this.connection = connection;
        this.queryUser = queryUser;
    }
}