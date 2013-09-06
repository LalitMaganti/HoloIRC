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

import android.content.Context;
import android.util.Log;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.UserChannelInterface;
import com.fusionx.lightirc.uiircinterface.MessageSender;
import com.fusionx.lightirc.util.IRCUtils;
import com.fusionx.lightirc.util.MiscUtils;

import java.util.ArrayList;

import static com.fusionx.lightirc.constants.Constants.DEBUG;
import static com.fusionx.lightirc.constants.Constants.LOG_TAG;
import static com.fusionx.lightirc.constants.ServerReplyCodes.ERR_NICKNAMEINUSE;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_ENDOFMOTD;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_ENDOFNAMES;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_ENDOFWHO;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_MOTD;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_MOTDSTART;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_NAMEREPLY;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_TOPIC;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_TOPICINFO;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_WHOREPLY;
import static com.fusionx.lightirc.constants.ServerReplyCodes.genericCodes;
import static com.fusionx.lightirc.constants.ServerReplyCodes.whoisCodes;
import static com.fusionx.lightirc.util.MiscUtils.isMotdAllowed;

public class ServerCodeParser {
    private final WhoParser mWhoParser;
    private final UserChannelInterface mUserChannelInterface;
    private final Context mContext;
    private final Server mServer;
    private final MessageSender mSender;
    private boolean motdAllowed;

    ServerCodeParser(final Context context, final ServerLineParser parser) {
        mServer = parser.getServer();
        mUserChannelInterface = mServer.getUserChannelInterface();
        mWhoParser = new WhoParser(mUserChannelInterface, mServer.getTitle());
        mContext = context;
        mSender = MessageSender.getSender(mServer.getTitle());
        motdAllowed = isMotdAllowed(mContext);
    }

    /**
     * The server is sending a code to us - parse what it is
     *
     * @param parsedArray - the array of the line (split by spaces)
     */
    void parseCode(final ArrayList<String> parsedArray) {
        final int code = Integer.parseInt(parsedArray.get(1));

        // Pretty common across all the codes
        MiscUtils.removeFirstElementFromList(parsedArray, 3);
        final String message = parsedArray.get(0);

        switch (code) {
            case RPL_NAMEREPLY:
            case RPL_ENDOFNAMES:
                // TODO - maybe try to use this rather than WHO replies in the future?
                return;
            case RPL_MOTDSTART:
            case RPL_MOTD:
                final String motdline = message.substring(1).trim();
                if (motdAllowed) {
                    mSender.sendGenericServerEvent(mServer, motdline);
                }
                return;
            case RPL_ENDOFMOTD:
                if (motdAllowed) {
                    mSender.sendGenericServerEvent(mServer, message);
                }
                return;
            case RPL_TOPIC:
                parseTopicReply(parsedArray);
                return;
            case RPL_TOPICINFO:
                parseTopicInfo(parsedArray);
                return;
            case RPL_WHOREPLY:
                mWhoParser.parseWhoReply(parsedArray);
                return;
            case RPL_ENDOFWHO:
                mWhoParser.parseWhoFinished();
                return;
            case ERR_NICKNAMEINUSE:
                final MessageSender sender = MessageSender.getSender(mServer.getTitle());
                sender.sendNickInUseMessage(mServer);
                return;
            default:
                if (whoisCodes.contains(code)) {
                    mSender.switchToServerMessage(mServer, MiscUtils.convertArrayListToString
                            (parsedArray));
                } else {
                    parseFallThroughCode(code, message);
                }
        }
    }

    private void parseTopicReply(ArrayList<String> parsedArray) {
        final String channelName = parsedArray.get(0);
        final String topic = parsedArray.get(1);
        final Channel channel = mUserChannelInterface.getChannel(channelName);
        channel.setTopic(topic);
    }

    // TODO - maybe using a colorful nick here if available?
    // TODO - possible optimization - make a new parser for topic stuff
    // Allows reduced overhead of retrieving channel from interface
    private void parseTopicInfo(final ArrayList<String> parsedArray) {
        final String channelName = parsedArray.get(0);
        final String nick = IRCUtils.getNickFromRaw(parsedArray.get(1));
        final Channel channel = mUserChannelInterface.getChannel(channelName);
        //channel.setTopicSetter(nick);

        mSender.sendGenericChannelEvent(channel,
                String.format(mContext.getString(R
                        .string.parser_new_topic), channel.getTopic(),
                        nick), false);
    }

    private void parseFallThroughCode(int code, String message) {
        if (genericCodes.contains(code)) {
            mSender.sendGenericServerEvent(mServer, message);
        } else if(DEBUG) {
            // Not sure what to do here - TODO
            Log.v(LOG_TAG, message);
        }
    }
}