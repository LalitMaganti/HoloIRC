package com.fusionx.lightirc.model;

public class EventDecorator {

    private final CharSequence mMessage;

    public EventDecorator(final CharSequence message) {
        mMessage = message;
    }

    public CharSequence getMessage() {
        return mMessage;
    }
}