/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.irc.parser;

import com.fusionx.lightirc.constants.ServerCommands;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.event.ErrorEvent;
import com.fusionx.lightirc.irc.event.Event;
import com.fusionx.lightirc.irc.event.QuitEvent;
import com.fusionx.lightirc.irc.misc.CoreListener;
import com.fusionx.lightirc.util.MiscUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ServerLineParser {
    @Getter(AccessLevel.PACKAGE)
    private final Server server;

    @Getter(AccessLevel.PACKAGE)
    @Setter
    private boolean disconnectSent;

    private final ServerCodeParser codeParser;
    private final ServerCommandParser commandParser;

    public ServerLineParser(final Server server) {
        this.server = server;
        commandParser = new ServerCommandParser(server.getContext(), this);
        codeParser = new ServerCodeParser(server.getContext(), this);
    }

    /**
     * A loop which reads each line from the server as it is received and passes it on
     * to parse
     *
     * @param reader - the reader associated with the server stream
     */
    public void parseMain(final BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            final Event quit = parseLine(line);
            if (quit instanceof QuitEvent || quit instanceof ErrorEvent) {
                return;
            }
        }
    }

    /**
     * Parses a line from the server
     *
     * @param line - the raw line from the server
     * @return - returns a boolean which indicates whether the server has disconnected
     */
    Event parseLine(final String line) {
        final ArrayList<String> parsedArray = MiscUtils.splitRawLine(line, true);
        switch (parsedArray.get(0)) {
            case ServerCommands.Ping:
                // Immediately return
                final String source = parsedArray.get(1);
                CoreListener.respondToPing(server.getWriter(), source);
                return new Event(line);
            case ServerCommands.Error:
                // We are finished - the server has kicked us out for some reason
                return new ErrorEvent(line);
            default:
                // Check if the second thing is a code or a command
                if (StringUtils.isNumeric(parsedArray.get(1))) {
                    return codeParser.parseCode(parsedArray);
                } else {
                    return commandParser.parseCommand(parsedArray, line, disconnectSent);
                }
        }
    }
}