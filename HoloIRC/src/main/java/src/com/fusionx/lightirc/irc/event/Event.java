package com.fusionx.lightirc.irc.event;

/**
 * A common class for all events to subclass
 */
public class Event {
    private String baseMessage = "";

    Event() {
    }

    public Event(String baseMessage) {
        this.baseMessage = baseMessage;
    }
}