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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.UserChannelInterface;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.event.Event;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.IRCUtils;
import com.fusionx.lightirc.util.MiscUtils;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import static com.fusionx.lightirc.constants.Constants.DEBUG;
import static com.fusionx.lightirc.constants.Constants.LOG_TAG;
import static com.fusionx.lightirc.constants.ServerReplyCodes.ERR_NICKNAMEINUSE;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_ENDOFMOTD;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_ENDOFNAMES;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_ENDOFWHO;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_MOTD;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_MOTDSTART;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_NAMREPLY;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_TOPIC;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_TOPICWHOTIME;
import static com.fusionx.lightirc.constants.ServerReplyCodes.RPL_WHOREPLY;
import static com.fusionx.lightirc.constants.ServerReplyCodes.doNothingCodes;
import static com.fusionx.lightirc.constants.ServerReplyCodes.genericCodes;
import static com.fusionx.lightirc.constants.ServerReplyCodes.whoisCodes;

class ServerCodeParser {

    private final WhoParser mWhoParser;

    private final NameParser mNameParser;

    private final UserChannelInterface mUserChannelInterface;

    private final Context mContext;

    private final Server mServer;

    private final MessageSender mSender;
    //private boolean motdAllowed;

    ServerCodeParser(final Context context, final ServerLineParser parser) {
        mServer = parser.getServer();
        mUserChannelInterface = mServer.getUserChannelInterface();
        mWhoParser = new WhoParser(mUserChannelInterface, mServer.getTitle());
        mNameParser = new NameParser(mUserChannelInterface, mServer.getTitle());
        mContext = context;
        mSender = MessageSender.getSender(mServer.getTitle());
        //motdAllowed = isMotdAllowed(mContext);
    }

    /**
     * The server is sending a code to us - parse what it is
     *
     * @param parsedArray - the array of the line (split by spaces)
     */
    Event parseCode(final ArrayList<String> parsedArray, final String rawLine) {
        final int code = Integer.parseInt(parsedArray.get(1));

        // Pretty common across all the codes
        MiscUtils.removeFirstElementFromList(parsedArray, 3);
        final String message = parsedArray.get(0);

        switch (code) {
            case RPL_NAMREPLY:
                return mNameParser.parseNameReply(parsedArray);
            case RPL_ENDOFNAMES:
                return mNameParser.parseNameFinished();
            case RPL_MOTDSTART:
            case RPL_MOTD:
                final String motdline = message.substring(1).trim();
                if (AppPreferences.motdAllowed) {
                    return mSender.sendGenericServerEvent(mServer, motdline);
                } else {
                    return new Event(motdline);
                }
            case RPL_ENDOFMOTD:
                if (AppPreferences.motdAllowed) {
                    return mSender.sendGenericServerEvent(mServer, message);
                } else {
                    return new Event(message);
                }
            case RPL_TOPIC:
                return parseTopicReply(parsedArray);
            case RPL_TOPICWHOTIME:
                return parseTopicInfo(parsedArray);
            case RPL_WHOREPLY:
                return mWhoParser.parseWhoReply(parsedArray);
            case RPL_ENDOFWHO:
                return mWhoParser.parseWhoFinished();
            case ERR_NICKNAMEINUSE:
                return mSender.sendNickInUseMessage(mServer);
            default:
                return parseFallThroughCode(code, message, rawLine, parsedArray);
        }
    }

    private Event parseTopicReply(ArrayList<String> parsedArray) {
        final String topic = parsedArray.get(1);
        final Channel channel = mUserChannelInterface.getChannel(parsedArray.get(0));
        channel.setTopic(topic);
        return new Event(topic);
    }

    // TODO - maybe using a colorful nick here if available?
    // TODO - possible optimization - make a new parser for topic stuff
    // Allows reduced overhead of retrieving channel from interface
    private ChannelEvent parseTopicInfo(final ArrayList<String> parsedArray) {
        final String channelName = parsedArray.get(0);
        final String nick = IRCUtils.getNickFromRaw(parsedArray.get(1));
        final Channel channel = mUserChannelInterface.getChannel(channelName);

        return mSender.sendGenericChannelEvent(channel, String.format(mContext.getString(R.string
                .parser_new_topic), channel.getTopic(), nick), false);
    }

    private Event parseFallThroughCode(final int code, final String message, final String rawLine,
            final ArrayList<String> parsedArray) {
        if (genericCodes.contains(code)) {
            return mSender.sendGenericServerEvent(mServer, message);
        } else if (whoisCodes.contains(code)) {
            return mSender.sendSwitchToServerEvent(mServer, MiscUtils.convertArrayListToString
                    (parsedArray));
        } else if (doNothingCodes.contains(code)) {
            return new Event(message);
        } else if (DEBUG) {
            // Not sure what to do here - TODO
            Log.v(LOG_TAG, rawLine);
        }
        return new Event(message);
    }
}