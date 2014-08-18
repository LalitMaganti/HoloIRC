package com.fusionx.lightirc.event;

import co.fusionx.relay.base.QueryUser;

public class OnQueryEvent {

    public final QueryUser queryUser;

    public OnQueryEvent(final QueryUser queryUser) {
        this.queryUser = queryUser;
    }
}