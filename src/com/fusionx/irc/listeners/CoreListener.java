package com.fusionx.irc.listeners;

import com.fusionx.irc.writers.ServerWriter;

public class CoreListener {
    public static void respondToPing(final ServerWriter serverWriter, final String serverName) {
        serverWriter.pongServer(serverName);
    }
}