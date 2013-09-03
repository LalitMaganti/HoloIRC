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

package com.fusionx.irc.parser.connection;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.fusionx.common.utils.Utils;
import com.fusionx.irc.constants.ServerCommands;
import com.fusionx.irc.core.Server;
import com.fusionx.irc.core.ServerConfiguration;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.listeners.CoreListener;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.core.MessageSender;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.fusionx.common.Constants.LOG_TAG;
import static com.fusionx.irc.constants.ServerReplyCodes.ERR_NICKNAMEINUSE;
import static com.fusionx.irc.constants.ServerReplyCodes.ERR_NONICKNAMEGIVEN;
import static com.fusionx.irc.constants.ServerReplyCodes.RPL_WELCOME;
import static com.fusionx.irc.constants.ServerReplyCodes.saslCodes;

public class ServerConnectionParser {
    private static boolean triedSecondNick = false;
    private static boolean triedThirdNick = false;
    private static int suffix = 0;

    public static String parseConnect(final Server server, final ServerConfiguration
            configuration, final BufferedReader reader, final Context context) throws
            IOException {

        String line;
        suffix = 0;
        triedSecondNick = false;
        triedThirdNick = false;
        final MessageSender sender = MessageSender.getSender(server.getTitle());

        while ((line = reader.readLine()) != null) {
            final ArrayList<String> parsedArray = Utils.splitRawLine(line, true);
            switch (parsedArray.get(0)) {
                case ServerCommands.Ping:
                    // Immediately return
                    final String source = parsedArray.get(1);
                    CoreListener.respondToPing(server.getWriter(), source);
                    break;
                case ServerCommands.Error:
                    // We are finished - the server has kicked us out for some reason
                    return null;
                case ServerCommands.Authenticate:
                    CapParser.parseCommand(parsedArray, configuration, server.getWriter(), sender);
                    break;
                default:
                    if (StringUtils.isNumeric(parsedArray.get(1))) {
                        final String nick = parseConnectionCode(configuration.isNickChangable(),
                                parsedArray, sender, server.getWriter(), context,
                                configuration.getNickStorage(), line);
                        if (nick != null) {
                            return nick;
                        }
                    } else {
                        parseConnectionCommand(parsedArray, configuration, sender,
                                server.getWriter(), line);
                    }
                    break;
            }
        }
        return null;
    }

    private static String parseConnectionCode(final boolean canChangeNick,
                                              final ArrayList<String> parsedArray,
                                              final MessageSender sender,
                                              final ServerWriter writer, final Context context,
                                              final ServerConfiguration.NickStorage nickStorage,
                                              final String line) {
        final int code = Integer.parseInt(parsedArray.get(1));
        switch (code) {
            case RPL_WELCOME:
                // We are now logged in.
                final String nick = parsedArray.get(2);
                Utils.removeFirstElementFromList(parsedArray, 3);
                sender.sendServerConnection(parsedArray.get(0));
                return nick;
            case ERR_NICKNAMEINUSE:
                if (!triedSecondNick && StringUtils.isNotEmpty(nickStorage.getSecondChoiceNick())) {
                    writer.changeNick(nickStorage.getSecondChoiceNick());
                    triedSecondNick = true;
                } else if (!triedThirdNick && StringUtils.isNotEmpty(nickStorage
                        .getThirdChoiceNick())) {
                    writer.changeNick(nickStorage.getThirdChoiceNick());
                    triedThirdNick = true;
                } else {
                    if (canChangeNick) {
                        ++suffix;
                        writer.changeNick(nickStorage.getFirstChoiceNick() + suffix);
                    } else {
                        final Bundle event = Utils.parcelDataForBroadcast(null,
                                ServerEventType.NickInUse,
                                context.getString(R.string.parser_nick_in_use));
                        sender.sendServerMessage(event);
                    }
                }
                break;
            case ERR_NONICKNAMEGIVEN:
                writer.changeNick(nickStorage.getFirstChoiceNick());
                break;
            default:
                if (saslCodes.contains(code)) {
                    CapParser.parseCode(code, parsedArray, sender, writer);
                } else {
                    Log.v(LOG_TAG, line);
                }
                break;
        }
        return null;
    }

    private static void parseConnectionCommand(final ArrayList<String> parsedArray,
                                               final ServerConfiguration configuration,
                                               final MessageSender sender, final ServerWriter writer,
                                               final String line) {
        switch (parsedArray.get(1).toUpperCase()) {
            case ServerCommands.Notice:
                Utils.removeFirstElementFromList(parsedArray, 3);
                sender.sendGenericServerEvent(parsedArray.get(0));
                break;
            case ServerCommands.Cap:
                Utils.removeFirstElementFromList(parsedArray, 3);
                CapParser.parseCommand(parsedArray, configuration, writer, sender);
                break;
            default:
                Log.v(LOG_TAG, line);
                break;
        }
    }

    /**
     * Not to be instantiated
     */
    private ServerConnectionParser() {
    }
}
