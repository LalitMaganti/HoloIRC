package com.fusionx.lightirc.irc.event;

public class MentionEvent {
    public final String destination;

    public MentionEvent(String destination) {
        this.destination = destination;
    }
}
