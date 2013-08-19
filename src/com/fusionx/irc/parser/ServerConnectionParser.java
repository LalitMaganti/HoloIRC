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

package com.fusionx.irc.parser;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.fusionx.Utils;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.misc.NickStorage;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.fusionx.irc.constants.Constants.LOG_TAG;
import static com.fusionx.irc.constants.ServerReplyCodes.ERR_NICKNAMEINUSE;
import static com.fusionx.irc.constants.ServerReplyCodes.RPL_WELCOME;

public class ServerConnectionParser {
    public static String parseConnect(final String title, final BufferedReader reader,
                                      final Context context, final boolean canChangeNick,
                                      final ServerWriter writer,
                                      final NickStorage nickStorage) throws IOException {
        String line;
        boolean triedSecondNick = false;
        boolean triedThirdNick = false;
        int suffix = 0;
        final MessageSender sender = MessageSender.getSender(title);
        while ((line = reader.readLine()) != null) {
            final ArrayList<String> parsedArray = Utils.splitRawLine(line, true);
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
                        if (!triedSecondNick) {
                            writer.changeNick(nickStorage.getSecondChoiceNick());
                            triedSecondNick = true;
                        } else if (!triedThirdNick) {
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
