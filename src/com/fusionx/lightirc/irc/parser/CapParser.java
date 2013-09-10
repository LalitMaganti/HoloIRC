package com.fusionx.lightirc.irc.parser;

import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.constants.ServerReplyCodes;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.writers.ServerWriter;
import com.fusionx.lightirc.util.MiscUtils;

import java.util.ArrayList;

class CapParser {
    static void parseCommand(final ArrayList<String> parsedArray, final ServerConfiguration
            configuration, final Server server, final MessageSender sender) {
        final ServerWriter writer = server.getWriter();
        final String command = parsedArray.get(0);
        if (command.equals("AUTHENTICATE")) {
            writer.sendSaslAuthentication(configuration.getSaslUsername(),
                    configuration.getSaslPassword());
        } else {
            final ArrayList<String> capabilities = MiscUtils.splitRawLine(parsedArray.get(1),
                    true);
            if (capabilities.contains("sasl")) {
                if (command.equals("LS")) {
                    writer.requestSasl();
                } else if (command.equals("ACK")) {
                    writer.sendPlainSaslAuthentication();
                }
            } else {
                sender.sendGenericServerEvent(server, "SASL not supported by server");
                writer.sendEndCap();
            }
        }
    }

    static void parseCode(final int code, final ArrayList<String> parsedArray,
                          final MessageSender sender, final Server server) {
        final ServerWriter writer = server.getWriter();
        switch (code) {
            case ServerReplyCodes.RPL_SASL_SUCCESSFUL:
                final String successful = parsedArray.get(3);
                sender.sendGenericServerEvent(server, successful);
                writer.sendEndCap();
                return;
            case ServerReplyCodes.RPL_SASL_LOGGED_IN:
                final String loginMessage = parsedArray.get(5);
                sender.sendGenericServerEvent(server, loginMessage);
                writer.sendEndCap();
                return;
            case ServerReplyCodes.ERR_SASL_FAILED:
            case ServerReplyCodes.ERR_SASL_FAILED_2:
                final String error = parsedArray.get(3);
                sender.sendGenericServerEvent(server, error);
                writer.sendEndCap();
                return;
            default:
                return;
        }
    }
}