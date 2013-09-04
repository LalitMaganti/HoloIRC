package com.fusionx.lightirc.irc.parser.connection;

import com.fusionx.lightirc.utils.Utils;
import com.fusionx.lightirc.irc.constants.ServerReplyCodes;
import com.fusionx.lightirc.irc.core.ServerConfiguration;
import com.fusionx.lightirc.irc.writers.ServerWriter;
import com.fusionx.lightirc.uiircinterface.core.MessageSender;

import java.util.ArrayList;

public class CapParser {
    static void parseCommand(final ArrayList<String> parsedArray, final ServerConfiguration
            configuration, final ServerWriter writer, final MessageSender sender) {
        final String command = parsedArray.get(0);
        if (command.equals("AUTHENTICATE")) {
            writer.sendSaslAuthentication(configuration.getSaslUsername(),
                    configuration.getSaslPassword());
        } else {
            final ArrayList<String> capabilities = Utils.splitRawLine(parsedArray.get(1),
                    true);
            if (capabilities.contains("sasl")) {
                if (command.equals("LS")) {
                    writer.requestSasl();
                } else if (command.equals("ACK")) {
                    writer.sendPlainSaslAuthentication();
                }
            } else {
                sender.sendGenericServerEvent("SASL not supported by server");
                writer.sendEndCap();
            }
        }
    }

    static boolean parseCode(final int code, final ArrayList<String> parsedArray,
                             final MessageSender sender, final ServerWriter writer) {
        switch (code) {
            case ServerReplyCodes.RPL_SASL_SUCCESSFUL:
                final String successful = parsedArray.get(3);
                sender.sendGenericServerEvent(successful);
                writer.sendEndCap();
                return true;
            case ServerReplyCodes.RPL_SASL_LOGGED_IN:
                final String loginMessage = parsedArray.get(5);
                sender.sendGenericServerEvent(loginMessage);
                writer.sendEndCap();
                return true;
            case ServerReplyCodes.ERR_SASL_FAILED:
            case ServerReplyCodes.ERR_SASL_FAILED_2:
                final String error = parsedArray.get(3);
                sender.sendGenericServerEvent(error);
                writer.sendEndCap();
                return false;
            default:
                return false;
        }
    }
}