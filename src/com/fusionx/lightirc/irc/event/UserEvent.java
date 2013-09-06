package com.fusionx.lightirc.irc.event;

public class UserEvent {
    public final String userNick;
    public final String message;

    public UserEvent(String userNick, String message) {
        this.userNick = userNick;
        this.message = message;
    }
}
