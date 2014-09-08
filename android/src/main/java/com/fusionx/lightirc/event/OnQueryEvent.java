package com.fusionx.lightirc.event;

import co.fusionx.relay.base.IRCSession;
import co.fusionx.relay.base.QueryUser;

public class OnQueryEvent {

    public final IRCSession connection;

    public final QueryUser queryUser;

    public OnQueryEvent(final IRCSession connection, final QueryUser queryUser) {
        this.connection = connection;
        this.queryUser = queryUser;
    }
}