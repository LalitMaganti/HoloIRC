package com.fusionx.ircinterface.parser;

import android.content.Context;
import android.util.Log;
import com.fusionx.ircinterface.Channel;
import com.fusionx.ircinterface.UserChannelInterface;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.misc.Utils;
import com.fusionx.lightirc.R;

import java.util.ArrayList;

import static com.fusionx.ircinterface.constants.Constants.LOG_TAG;
import static com.fusionx.ircinterface.constants.ServerReplyCodes.*;
import static com.fusionx.lightirc.misc.Utils.isMotdAllowed;

public class ServerCodeParser {
    private final StringBuilder mStringBuilder = new StringBuilder();
    private final WhoParser mWhoParser;
    private final ServerLineParser mMainParser;
    private final UserChannelInterface mUserChannelInterface;
    private final Context mContext;

    ServerCodeParser(final Context context, ServerLineParser parser) {
        mUserChannelInterface = parser.getServer().getUserChannelInterface();
        mWhoParser = new WhoParser(mUserChannelInterface);
        mMainParser = parser;
        mContext = context;
    }

    /**
     * The server is sending a code to us - parse what it is
     *
     * @param parsedArray - the array of the line (split by spaces)
     */
    void parseCode(final ArrayList<String> parsedArray) {
        final int code = Integer.parseInt(parsedArray.get(1));

        // Pretty common across all the codes
        Utils.removeFirstElementFromList(parsedArray, 3);
        final String message = parsedArray.get(0);

        switch (code) {
            case RPL_MOTDSTART:
                mStringBuilder.setLength(0);
                // Fall through here to RPL_MOTD case is intentional
            case RPL_MOTD:
                mStringBuilder.append(message.substring(1).trim()).append("\n");
                return;
            case RPL_TOPIC:
                parseTopicReply(parsedArray);
                return;
            case RPL_TOPICINFO:
                parseTopicInfo(parsedArray);
                return;
            case RPL_ENDOFMOTD:
                parseMOTDFinished(message);
                return;
            case RPL_WHOREPLY:
                mWhoParser.parseWhoReply(parsedArray);
                return;
            case RPL_ENDOFWHO:
                mWhoParser.parseWhoFinished();
                return;
            default:
                parseFallThroughCode(code, message);
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
        final String nick = Utils.getNickFromRaw(parsedArray.get(1));
        final Channel channel = mUserChannelInterface.getChannel(channelName);
        channel.setTopicSetter(nick);

        // TODO find if there is a better way to do this
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mMainParser.getBroadcastSender().broadcastGenericChannelEvent(channel.getName(),
                String.format(mContext.getString(R.string.parser_new_topic),
                        channel.getTopic(), nick));
    }

    private void parseFallThroughCode(int code, String message) {
        if (genericCodes.contains(code)) {
            final Event event = Utils.parcelDataForBroadcast(EventDestination.Server,
                    null, ServerEventType.Generic, message);
            mMainParser.getBroadcastSender().sendBroadcast(event);
        } else {
            // Not sure what to do here - TODO
            Log.v(LOG_TAG, message);
        }
    }

    /**
     * Packs up MOTD, adds it to the server details and sends a generic server event (if allowed)
     *
     * @param message - the final line of the MOTD command
     */
    private void parseMOTDFinished(String message) {
        mStringBuilder.append(message);

        final String MOTD = mStringBuilder.toString().trim();
        mMainParser.getServer().setMOTD(MOTD);

        if(isMotdAllowed(mContext)) {
            final Event event = Utils.parcelDataForBroadcast(EventDestination.Server,
                    null, ServerEventType.Generic, MOTD);
            mMainParser.getBroadcastSender().sendBroadcast(event);
        }
    }
}