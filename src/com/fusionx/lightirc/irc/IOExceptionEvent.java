package com.fusionx.lightirc.irc;

import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;

import java.io.IOException;

public class IOExceptionEvent<T extends PircBotX> extends Event<T> {
    @Getter(AccessLevel.PUBLIC)
    private final IOException exception;

    public IOExceptionEvent(T bot, IOException exception) {
        super(bot);
        this.exception = exception;
    }

    @Override
    public void respond(String response) {
        // do nothing
    }
}