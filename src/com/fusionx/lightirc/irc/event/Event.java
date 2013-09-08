package com.fusionx.lightirc.irc.event;

/**
 * A common class for all events to subclass
 */
public class Event {
    public String message = "";

    Event() {
    }

    public Event(String message) {
        this.message = message;
    }
}