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
import com.fusionx.irc.AppUser;
import com.fusionx.irc.Channel;
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.PrivateMessageUser;
import com.fusionx.irc.Server;
import com.fusionx.irc.UserChannelInterface;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

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
                    final String strippedMessage = message.substring(1, message.length() - 1);
                    parseCTCPCommand(parsedArray, strippedMessage, rawSource);
                } else {
                    parsePRIVMSGCommand(parsedArray, rawSource);
                }
                return;
            case "JOIN":
                parseChannelJoin(parsedArray, rawSource);
                return;
            case "NOTICE":
                parseNotice(parsedArray, rawSource);
                return;
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
                Log.v(LOG_TAG, rawLine);
                // Not sure what to do here - TODO
        }
    }

    private void parseNotice(final ArrayList<String> parsedArray, final String rawSource) {
        final String sendingUser = Utils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String notice = parsedArray.get(3);

        final String formattedNotice = String.format(mContext.getString(R.string
                .parser_message), sendingUser, notice);
        if (Utils.isChannel(recipient)) {
            mSender.sendGenericChannelEvent(recipient, formattedNotice);
        } else if (recipient.equals(mServer.getUser().getNick())) {
            final PrivateMessageUser user = mServer.getPrivateMessageUser(sendingUser);
            if (mServer.getUser().isPrivateMessageOpen(user)) {
                mServer.privateMessageSent(user, notice, false);
            } else {
                mSender.sendGenericServerEvent(formattedNotice);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void parseCTCPCommand(final ArrayList<String> parsedArray, final String message,
                                  final String rawSource) {
        if (message.startsWith("ACTION ")) {
            parseAction(parsedArray, rawSource);
        } else if (message.startsWith("VERSION")) {
            // TODO - figure out what should be done here
        } else {
            // TODO - add more things here
            throw new IllegalStateException();
        }
    }

    private void parsePRIVMSGCommand(final ArrayList<String> parsedArray, final String rawSource) {
        final String nick = Utils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String message = parsedArray.get(3);

        if (Utils.isChannel(recipient)) {
            final ChannelUser sendingUser = mUserChannelInterface.getUser(nick);
            final Channel channel = mUserChannelInterface.getChannel(recipient);
            mSender.sendMessageToChannel(channel, sendingUser, message);
        } else {
            final PrivateMessageUser sendingUser = mServer.getPrivateMessageUser(nick);
            mServer.privateMessageSent(sendingUser, message, false);
        }
    }

    private void parseAction(ArrayList<String> parsedArray, String rawSource) {
        final String nick = Utils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String action = parsedArray.get(3).replace("ACTION ", "");

        if (Utils.isChannel(recipient)) {
            final ChannelUser sendingUser = mUserChannelInterface.getUser(nick);
            mSender.sendChannelAction(recipient, sendingUser, action);
        } else {
            final PrivateMessageUser sendingUser = mServer.getPrivateMessageUser(nick);
            mServer.privateActionSent(sendingUser, action, false);
        }
    }

    private void parseTopicChange(final ArrayList<String> parsedArray, final String rawSource) {
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
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
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
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
                final ChannelUser user = mUserChannelInterface.getUser(userRecipient);

                final String message = user.processModeChange(mContext, sendingUser, channel, mode);

                mSender.sendGenericUserListChangedEvent(channel.getName(), message);
            }
        } else {
            // A user is changing a mode about themselves
            // TODO - implement this?
        }
    }

    private void parseChannelJoin(final ArrayList<String> parsedArray, final String rawSource) {
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
        final Channel channel = mUserChannelInterface.getChannel(parsedArray.get(2));
        mUserChannelInterface.coupleUserAndChannel(user, channel);

        if (user.equals(mServer.getUser())) {
            mSender.sendChanelJoined(channel.getName());
            channel.getWriter().sendWho();
        } else {
            mSender.sendGenericUserListChangedEvent(channel.getName(),
                    String.format(mContext
                            .getString(R.string.parser_joined_channel), user.getPrettyNick(channel)));
            //user.getWriter().sendWho();
        }
    }

    private void parseChannelPart(final ArrayList<String> parsedArray, final String rawSource) {
        final String channelName = parsedArray.get(2);

        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
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
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
        if (user.equals(mServer.getUser())) {
            // TODO - improve this
            mSender.sendServerDisconnection("");
        } else {
            for (final Channel channel : mUserChannelInterface.removeUser(user)) {
                final String message = String.format(mContext.getString(R.string.parser_quit_server),
                        user.getPrettyNick(channel)) +
                        // If you have 3 strings in the array, the last must be the reason for quitting
                        ((parsedArray.size() == 3) ? " " +
                                String.format(mContext.getString(R.string.parser_reason),
                                        StringUtils.remove(parsedArray.get(2), "\"")) : "");
                mSender.sendGenericUserListChangedEvent(channel.getName(), message);
            }
        }
    }
}