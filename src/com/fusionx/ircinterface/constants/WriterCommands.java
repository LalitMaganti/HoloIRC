package com.fusionx.ircinterface.constants;

public final class WriterCommands {
    public final static String PRIVMSG = "PRIVMSG %1$s :%2$s";
    public final static String PART = "PART %1$s :%2$s";
    public final static String WHO = "WHO %1$s";
    public final static String ACTION = "PRIVMSG %1$s :\u0001ACTION %2$s\u0001";

    private WriterCommands() {
    }
}