package com.fusionx.lightirc.event;

import co.fusionx.relay.base.Session;
import co.fusionx.relay.base.QueryUser;

public class OnQueryEvent {

    public final Session connection;

    public final QueryUser queryUser;

    public OnQueryEvent(final Session connection, final QueryUser queryUser) {
        this.connection = connection;
        this.queryUser = queryUser;
    }
}