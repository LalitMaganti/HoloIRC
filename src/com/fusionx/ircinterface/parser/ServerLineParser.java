package com.fusionx.ircinterface.parser;

import android.content.Context;
import android.util.Log;
import com.fusionx.ircinterface.Server;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.constants.ServerCommands;
import com.fusionx.ircinterface.enums.CoreEventType;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.misc.BroadcastSender;
import com.fusionx.ircinterface.misc.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.fusionx.ircinterface.constants.Constants.LOG_TAG;

@Getter(AccessLevel.PACKAGE)
public class ServerLineParser {
    private final BroadcastSender broadcastSender;
    private final Server server;

    @Getter(AccessLevel.NONE)
    private final ServerCodeParser codeParser;
    @Getter(AccessLevel.NONE)
    private final ServerCommandParser commandParser;

    public ServerLineParser(final BroadcastSender broadcastSender, final Server server,
                            final Context context) {
        this.broadcastSender = broadcastSender;
        this.server = server;
        commandParser = new ServerCommandParser(context, this);
        codeParser = new ServerCodeParser(context, this);
    }

    /**
     * A loop which reads each line from the server as it is received and passes it on
     * to parse
     *
     * @param reader - the reader associated with the server stream
     */
    public void parseMain(final BufferedReader reader) {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                final ServerEventType type = parseLine(line);
                if (type != null && type.equals(ServerEventType.Error)) {
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Parses a line from the server
     *
     * @param line - the raw line from the server
     * @return returns an event if there is an error - otherwise returns null
     */
    ServerEventType parseLine(final String line) {
        final ArrayList<String> parsedArray = Utils.splitRawLine(line);
        switch (parsedArray.get(0)) {
            case ServerCommands.Ping: {
                // Immediately return
                final String source = parsedArray.get(1);
                final Event event = Utils.parcelDataForBroadcast(EventDestination.Core, null,
                        CoreEventType.Ping, source);
                broadcastSender.sendBroadcast(event);
                return null;
            }
            case ServerCommands.Error: {
                // We are finished - the server has kicked us out for some reason
                final Event event = Utils.parcelDataForBroadcast(EventDestination.Server, null,
                        ServerEventType.Error, parsedArray.get(1));
                broadcastSender.sendBroadcast(event);
                return ServerEventType.Error;
            }
            default: {
                // Check if the second thing is a code or a command
                if (StringUtils.isNumeric(parsedArray.get(1))) {
                    codeParser.parseCode(parsedArray);
                } else {
                    commandParser.parseCommand(parsedArray, line);
                }
                return null;
            }
        }
    }
}