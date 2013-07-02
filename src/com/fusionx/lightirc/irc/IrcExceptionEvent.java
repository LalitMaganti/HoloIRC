package com.fusionx.lightirc.irc;

import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.Event;

public class IrcExceptionEvent<T extends PircBotX> extends Event<T> {
    @Getter(AccessLevel.PUBLIC)
    private final IrcException exception;

    public IrcExceptionEvent(T bot, IrcException exception) {
        super(bot);
        this.exception = exception;
    }

    @Override
    public void respond(String response) {
        // Invalid
        throw new UnsupportedOperationException();
    }
}
