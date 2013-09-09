package com.fusionx.lightirc.irc.event;

/**
 * A common class for all events to subclass
 */
public class Event {
    public String baseMessage = "";

    Event() {
    }

    public Event(String baseMessage) {
        this.baseMessage = baseMessage;
    }
}