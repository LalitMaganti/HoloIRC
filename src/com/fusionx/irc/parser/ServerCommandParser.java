package com.fusionx.irc.parser;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.fusionx.irc.AppUser;
import com.fusionx.irc.Channel;
import com.fusionx.irc.Server;
import com.fusionx.irc.User;
import com.fusionx.irc.UserChannelInterface;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.misc.Utils;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.MessageSender;

import java.util.ArrayList;
import java.util.Set;

import de.scrum_master.util.UpdateableTreeSet;

import static com.fusionx.irc.constants.Constants.LOG_TAG;

public class ServerCommandParser {
    private final Context mContext;
    private final UserChannelInterface mUserChannelInterface;
    private final Server mServer;
    private final MessageSender mSender;

    ServerCommandParser(final Context context, ServerLineParser parser) {
        mContext = context;
        mServer = parser.getServer();
        mUserChannelInterface = mServer.getUserChannelInterface();
        mSender = MessageSender.getSender(mServer.getTitle());
    }

    // The server is sending a command to us - parse what it is
    void parseCommand(final ArrayList<String> parsedArray, final String rawLine) {
        final String rawSource = parsedArray.get(0);
        final String command = parsedArray.get(1).toUpperCase();

        switch (command) {
            case "PRIVMSG":
                final String message = parsedArray.get(3);
                if (message.startsWith("\u0001") && message.endsWith("\u0001")) {
                    parseCTCPCommand(parsedArray, message
                            .substring(1, message.length() - 1), rawSource);
                } else {
                    parsePRIVMSGCommand(parsedArray, rawSource);
                }
                return;
            case "JOIN":
                parseChannelJoin(parsedArray, rawSource);
                return;
            //case "NOTICE":
            //    throw new UnsupportedOperationException();
            case "PART":
                parseChannelPart(parsedArray, rawSource);
                return;
            case "MODE":
                parseModeChange(parsedArray, rawSource);
                return;
            case "QUIT":
                parseServerQuit(parsedArray, rawSource);
                return;
            case "NICK":
                parseNickChange(parsedArray, rawSource);
                return;
            case "TOPIC":
                parseTopicChange(parsedArray, rawSource);
                return;
            default:
                // Not sure what to do here - TODO
                Log.v(LOG_TAG, rawLine);
        }
    }

    private void parseCTCPCommand(final ArrayList<String> parsedArray, final String message,
                                  final String rawSource) {
        if (message.startsWith("ACTION ")) {
            parseAction(parsedArray, rawSource);
        } else {
            // TODO - add more things here
            throw new IllegalStateException();
        }
    }

    private void parsePRIVMSGCommand(final ArrayList<String> parsedArray, final String rawSource) {
        final User sendingUser = mUserChannelInterface.getUserFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String message = parsedArray.get(3);

        if (Utils.isChannel(recipient)) {
            // PRIVMSG to channel
            final Channel channel = mUserChannelInterface.getChannel(recipient);
            mSender.sendAppUserMessageToChannel(channel,
                    sendingUser, message);
        } else {
            // PRIVMSG to user
            if (!mServer.getUser().getPrivateMessages().contains(sendingUser)) {
                mServer.getUser().newPrivateMessage(sendingUser);
                sendingUser.registerHandler();

                mSender.sendPrivateMessage(sendingUser, message);

                final Bundle event = Utils.parcelDataForBroadcast(null,
                        ServerEventType.NewPrivateMessage, sendingUser.getNick());
                mSender.sendServerMessage(event);
            } else {
                mSender.sendPrivateMessage(sendingUser, message);
            }
        }
    }

    private void parseAction(ArrayList<String> parsedArray, String rawSource) {
        final User sendingUser = mUserChannelInterface.getUserFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String message = parsedArray.get(3).replace("ACTION ", "");
        if (Utils.isChannel(recipient)) {
            mSender.sendAction(recipient, sendingUser, message);
        } else {
            if (!mServer.getUser().getPrivateMessages().contains(sendingUser)) {
                mServer.getUser().newPrivateMessage(sendingUser);
                sendingUser.registerHandler();

                mSender.sendAction(sendingUser.getNick(), sendingUser, message);

                final Bundle event = Utils.parcelDataForBroadcast(null,
                        ServerEventType.NewPrivateMessage, sendingUser.getNick());
                mSender.sendServerMessage(event);
            } else {
                mSender.sendAction(sendingUser.getNick(), sendingUser, message);
            }
        }
    }

    private void parseTopicChange(ArrayList<String> parsedArray, String rawSource) {
        final User user = mUserChannelInterface.getUserFromRaw(rawSource);
        final Channel channel = mUserChannelInterface.getChannel(parsedArray.get(2));
        final String setterNick = user.getPrettyNick(channel);
        final String newTopic = parsedArray.get(3);

        final String message = String
                .format(mContext.getString(R.string.parser_topic_changed, newTopic, setterNick));
        channel.setTopic(newTopic);
        channel.setTopicSetter(user.getNick());

        mSender.sendGenericChannelEvent(channel.getName(), message);
    }

    private void parseNickChange(ArrayList<String> parsedArray, String rawSource) {
        final User user = mUserChannelInterface.getUserFromRaw(rawSource);
        final UpdateableTreeSet<Channel> channels = user.getChannels();
        final String oldNick = user.getColorfulNick();
        user.setNick(parsedArray.get(2));

        String message = user instanceof AppUser ?
                String.format(mContext.getString(R.string.parser_appuser_nick_changed),
                        oldNick, user.getColorfulNick()) :
                String.format(mContext.getString(R.string.parser_other_user_nick_change),
                        oldNick, user.getColorfulNick());

        for (final Channel channel : channels) {
            mSender.sendGenericChannelEvent(channel.getName(), message);
            channel.getUsers().update(user);
        }
    }

    private void parseModeChange(final ArrayList<String> parsedArray, final String rawSource) {
        final String sendingUser = Utils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String mode = parsedArray.get(3);
        if (Utils.isChannel(recipient)) {
            // The recipient is a channel (i.e. the mode of a user in the channel is being changed
            // or possibly the mode of the channel itself)
            if (parsedArray.size() == 4) {
                // User not specified - therefore channel mode is being changed
                // TODO - implement this?
            } else if (parsedArray.size() == 5) {
                // User specified - therefore user mode in channel is being changed
                final Channel channel = mUserChannelInterface.getChannel(recipient);
                final String userRecipient = parsedArray.get(4);
                final User user = mUserChannelInterface.getUser(userRecipient);

                final String message = user.processModeChange(mContext, sendingUser, channel, mode);

                mSender.sendGenericUserListChangedEvent(channel.getName(), message);
            }
        } else {
            // A user is changing a mode about themselves
            // TODO - implement this?
        }
    }

    private void parseChannelJoin(final ArrayList<String> parsedArray, final String rawSource) {
        final User user = mUserChannelInterface.getUserFromRaw(rawSource);
        final Channel channel = mUserChannelInterface.getChannel(parsedArray.get(2));
        mUserChannelInterface.coupleUserAndChannel(user, channel);

        if (user.equals(mServer.getUser())) {
            mSender.sendChanelJoined(channel.getName());
            channel.getWriter().sendWho();
        } else {
            mSender.sendGenericUserListChangedEvent(channel.getName(),
                    String.format(mContext
                            .getString(R.string.parser_joined_channel), user.getPrettyNick(channel)));
            user.getWriter().sendWho();
        }
    }

    private void parseChannelPart(final ArrayList<String> parsedArray, final String rawSource) {
        final String channelName = parsedArray.get(2);

        final User user = mUserChannelInterface.getUserFromRaw(rawSource);
        final Channel channel = mUserChannelInterface.getChannel(channelName);
        if (user.equals(mServer.getUser())) {
            Log.e(LOG_TAG, Utils.convertArrayListToString(parsedArray));
            mSender.sendChanelParted(channelName);
            mUserChannelInterface.removeChannel(channel);
        } else {
            String message = String.format(mContext.getString(R.string.parser_parted_channel),
                    user.getPrettyNick(channel));
            // If you have 4 strings in the array, the last must be the reason for parting
            message += (parsedArray.size() == 4) ? " " +
                    String.format(mContext.getString(R.string.parser_reason),
                            parsedArray.get(3).replace("\"", "")) : "";

            mSender.sendGenericUserListChangedEvent(channelName, message);
            mUserChannelInterface.decoupleUserAndChannel(user, channel);
        }
    }

    private void parseServerQuit(final ArrayList<String> parsedArray, final String rawSource) {
        final User user = mUserChannelInterface.getUserFromRaw(rawSource);
        if (user.equals(mServer.getUser())) {
            //final Event joinEvent = Utils.parcelDataForBroadcast(EventDestination., channelName,
            //        ServerEventType.Generic, message);
            //mainParser.getBroadcastSender().sendBroadcast(joinEvent);
            // TODO - unexpected disconnect?
        } else {
            // If you have 3 strings in the array, the last must be the reason for quitting
            String message = (parsedArray.size() == 3) ? " " +
                    String.format(mContext.getString(R.string.parser_reason),
                            parsedArray.get(2).replace("\"", "")) : "";

            final Set<Channel> channels = mUserChannelInterface.removeUser(user);
            for (Channel channel : channels) {
                message = String.format(mContext.getString(R.string.parser_quit_server),
                        user.getPrettyNick(channel)) + message;
                mSender.sendGenericUserListChangedEvent(channel.getName(), message);
            }
        }
    }
}