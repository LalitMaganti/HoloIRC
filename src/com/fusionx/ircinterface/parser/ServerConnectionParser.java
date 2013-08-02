package com.fusionx.ircinterface.parser;

import android.util.Log;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.misc.BroadcastSender;
import com.fusionx.ircinterface.misc.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.fusionx.ircinterface.constants.Constants.LOG_TAG;
import static com.fusionx.ircinterface.constants.ServerReplyCodes.ERR_NICKNAMEINUSE;
import static com.fusionx.ircinterface.constants.ServerReplyCodes.RPL_WELCOME;

public class ServerConnectionParser {
    public static String parseConnect(final BroadcastSender broadcastSender,
                                      final BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            final ArrayList<String> parsedArray = Utils.splitRawLine(line);
            if (StringUtils.isNumeric(parsedArray.get(1))) {
                switch (Integer.parseInt(parsedArray.get(1))) {
                    case RPL_WELCOME: {
                        // We are now logged in.
                        final String nick = parsedArray.get(2);
                        Utils.removeFirstElementFromList(parsedArray, 3);
                        broadcastSender.broadcastServerConnection(parsedArray.get(0));
                        return nick;
                    }
                    case ERR_NICKNAMEINUSE: {
                        final Event event = Utils.parcelDataForBroadcast(EventDestination.Server,
                                null, ServerEventType.NickInUse, "Nickname is already in use.");
                        broadcastSender.sendBroadcast(event);
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
                        broadcastSender.broadcastGenericServerEvent(parsedArray.get(0));
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
