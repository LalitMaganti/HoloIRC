package app.holoirc.event;

import co.fusionx.relay.base.QueryUser;

public class OnQueryEvent {

    public final QueryUser queryUser;
    public final String message;
    public final long timestamp;

    public OnQueryEvent(final QueryUser queryUser, final String message, final long timestamp) {
        this.queryUser = queryUser;
        this.message = message;
        this.timestamp = timestamp;
    }
}