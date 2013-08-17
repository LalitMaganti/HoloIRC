package com.fusionx.irc.parser;

import android.os.Bundle;
import android.util.Log;

import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.uiircinterface.MessageSender;
import com.fusionx.irc.misc.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.fusionx.irc.constants.Constants.LOG_TAG;
import static com.fusionx.irc.constants.ServerReplyCodes.ERR_NICKNAMEINUSE;
import static com.fusionx.irc.constants.ServerReplyCodes.RPL_WELCOME;

public class ServerConnectionParser {
    public static String parseConnect(final String title, final BufferedReader reader) throws
            IOException {
        String line;
        final MessageSender sender = MessageSender.getSender(title);
        while ((line = reader.readLine()) != null) {
            final ArrayList<String> parsedArray = Utils.splitRawLine(line);
            if (StringUtils.isNumeric(parsedArray.get(1))) {
                switch (Integer.parseInt(parsedArray.get(1))) {
                    case RPL_WELCOME: {
                        // We are now logged in.
                        final String nick = parsedArray.get(2);
                        Utils.removeFirstElementFromList(parsedArray, 3);
                        sender.sendServerConnection(parsedArray.get(0));
                        return nick;
                    }
                    case ERR_NICKNAMEINUSE: {
                        final Bundle event = Utils.parcelDataForBroadcast(null,
                                ServerEventType.NickInUse, "Nickname is already in use.");
                        sender.sendServerMessage(event);
                        break;
                    }
                    default: {
                        Log.v(LOG_TAG, line);
                        break;
                    }
                }
            } else {
                switch (parsedArray.get(1).toUpperCase()) {
                    case "NOTICE": {
                        Utils.removeFirstElementFromList(parsedArray, 3);
                        sender.sendGenericServerEvent(parsedArray.get(0));
                        break;
                    }
                    default: {
                        Log.v(LOG_TAG, line);
                        break;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Not to be instantiated
     */
    private ServerConnectionParser() {
    }
}
