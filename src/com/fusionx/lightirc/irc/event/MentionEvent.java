package com.fusionx.lightirc.irc.event;

public class MentionEvent extends Event {
    public final String destination;

    public MentionEvent(String destination) {
        this.destination = destination;
    }
}
