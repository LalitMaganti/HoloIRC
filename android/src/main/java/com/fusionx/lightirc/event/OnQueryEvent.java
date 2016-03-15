package com.fusionx.lightirc.event;

import co.fusionx.relay.QueryUser;

public class OnQueryEvent {

    public final QueryUser queryUser;

    public OnQueryEvent(final QueryUser queryUser) {
        this.queryUser = queryUser;
    }
}