package com.fusionx.lightirc.event;

import co.fusionx.relay.base.QueryUser;

public class OnQueryEvent {

    public final QueryUser queryUser;
    public final String message;

    public OnQueryEvent(final QueryUser queryUser, final String message) {
        this.queryUser = queryUser;
        this.message = message;
    }
}