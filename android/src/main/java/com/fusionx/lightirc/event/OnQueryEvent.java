package com.fusionx.lightirc.event;

import co.fusionx.relay.base.IRCConnection;
import co.fusionx.relay.base.QueryUser;

public class OnQueryEvent {

    public final IRCConnection connection;

    public final QueryUser queryUser;

    public OnQueryEvent(final IRCConnection connection, final QueryUser queryUser) {
        this.connection = connection;
        this.queryUser = queryUser;
    }
}