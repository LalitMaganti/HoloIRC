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
import com.fusionx.lightirc.constants.ServerCommands;
import com.fusionx.lightirc.irc.AppUser;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.UserChannelInterface;
import com.fusionx.lightirc.uiircinterface.MessageSender;
import com.fusionx.lightirc.util.IRCUtils;
import com.fusionx.lightirc.util.MiscUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Set;

import de.scrum_master.util.UpdateableTreeSet;

import static com.fusionx.lightirc.constants.Constants.DEBUG;
import static com.fusionx.lightirc.constants.Constants.LOG_TAG;

public class ServerCommandParser {
    private final Context mContext;
    private final UserChannelInterface mUserChannelInterface;
    private final Server mServer;
    private final MessageSender mSender;

    ServerCommandParser(final Context context, ServerLineParser parser) {
        mServer = parser.getServer();

        mContext = context;
        mUserChannelInterface = mServer.getUserChannelInterface();

        mSender = MessageSender.getSender(mServer.getTitle());
    }

    // The server is sending a command to us - parse what it is
    boolean parseCommand(final ArrayList<String> parsedArray, final String rawLine,
                         final boolean disconnectSent) {
        final String rawSource = parsedArray.get(0);
        final String command = parsedArray.get(1).toUpperCase();

        switch (command) {
            case ServerCommands.Privmsg: {
                final String message = parsedArray.get(3);
                if (message.startsWith("\u0001") && message.endsWith("\u0001")) {
                    final String strippedMessage = message.substring(1, message.length() - 1);
                    parseCTCPCommand(parsedArray, strippedMessage, rawSource);
                } else {
                    parsePRIVMSGCommand(parsedArray, rawSource);
                }
                return false;
            }
            case ServerCommands.Join:
                parseChannelJoin(parsedArray, rawSource);
                return false;
            case ServerCommands.Notice:
                final String message = parsedArray.get(3);
                if (message.startsWith("\u0001") && message.endsWith("\u0001")) {
                    final String strippedMessage = message.substring(1, message.length() - 1);
                    parseCTCPCommand(parsedArray, strippedMessage, rawSource);
                } else {
                    parseNotice(parsedArray, rawSource);
                }
                return false;
            case ServerCommands.Part:
                parseChannelPart(parsedArray, rawSource, disconnectSent);
                return false;
            case ServerCommands.Mode:
                parseModeChange(parsedArray, rawSource);
                return false;
            case ServerCommands.Quit:
                return parseServerQuit(parsedArray, rawSource);
            case ServerCommands.Nick:
                parseNickChange(parsedArray, rawSource);
                return false;
            case ServerCommands.Topic:
                parseTopicChange(parsedArray, rawSource);
                return false;
            default:
                // Not sure what to do here - TODO
                if (DEBUG) {
                    Log.v(LOG_TAG, rawLine);
                }
                return false;
        }
    }

    private void parseNotice(final ArrayList<String> parsedArray, final String rawSource) {
        final String sendingUser = IRCUtils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String notice = parsedArray.get(3);

        final String formattedNotice = String.format(mContext.getString(R.string
                .parser_message), sendingUser, notice);
        if (MiscUtils.isChannel(recipient.charAt(0))) {
            mSender.sendGenericChannelEvent(mServer, mUserChannelInterface.getChannel(recipient),
                    formattedNotice, false);
        } else if (recipient.equals(mServer.getUser().getNick())) {
            final PrivateMessageUser user = mServer.getPrivateMessageUser(sendingUser);
            if (mServer.getUser().isPrivateMessageOpen(user)) {
                mServer.privateMessageSent(user, notice, false);
            } else {
                mSender.sendGenericServerEvent(mServer, formattedNotice);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void parseCTCPCommand(final ArrayList<String> parsedArray, final String message,
                                  final String rawSource) {
        if (message.startsWith("ACTION")) {
            parseAction(parsedArray, rawSource);
        } else if (message.startsWith("VERSION")) {
            final String nick = IRCUtils.getNickFromRaw(rawSource);
            mServer.getWriter().sendVersion(nick, mServer.toString());
            // TODO - figure out what should be done here
        } else {
            // TODO - add more things here
            throw new IllegalStateException();
        }
    }

    private void parsePRIVMSGCommand(final ArrayList<String> parsedArray, final String rawSource) {
        final String nick = IRCUtils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String message = parsedArray.get(3);

        // TODO - optimize this
        if (!MiscUtils.getIgnoreList(mContext, mServer.getTitle().toLowerCase()).contains(nick)) {
            if (MiscUtils.isChannel(recipient.charAt(0))) {
                final ChannelUser sendingUser = mUserChannelInterface.getUser(nick);
                final Channel channel = mUserChannelInterface.getChannel(recipient);
                mSender.sendMessageToChannel(mServer, mServer.getUser().getNick(), channel,
                        sendingUser.getPrettyNick(channel), message);
            } else {
                final PrivateMessageUser sendingUser = mServer.getPrivateMessageUser(nick);
                mServer.privateMessageSent(sendingUser, message, false);
            }
        }
    }

    private void parseAction(ArrayList<String> parsedArray, String rawSource) {
        final String nick = IRCUtils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String action = parsedArray.get(3).replace("ACTION ", "");

        if (!MiscUtils.getIgnoreList(mContext, mServer.getTitle().toLowerCase()).contains(nick)) {
            if (MiscUtils.isChannel(recipient.charAt(0))) {
                final ChannelUser sendingUser = mUserChannelInterface.getUser(nick);
                mSender.sendChannelAction(mServer, mServer.getUser().getNick(),
                        mUserChannelInterface.getChannel(recipient), sendingUser, action);
            } else {
                final PrivateMessageUser sendingUser = mServer.getPrivateMessageUser(nick);
                mServer.privateActionSent(sendingUser, action, false);
            }
        }
    }

    private void parseTopicChange(final ArrayList<String> parsedArray, final String rawSource) {
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
        final Channel channel = mUserChannelInterface.getChannel(parsedArray.get(2));
        final String setterNick = user.getPrettyNick(channel);
        final String newTopic = parsedArray.get(3);

        final String message = String
                .format(mContext.getString(R.string.parser_topic_changed, newTopic, setterNick));
        mSender.sendGenericChannelEvent(mServer, channel, message, false);
    }

    private void parseNickChange(ArrayList<String> parsedArray, String rawSource) {
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
        final UpdateableTreeSet<Channel> channels = user.getChannels();
        final String oldNick = user.getColorfulNick();
        user.setNick(parsedArray.get(2));

        final String message = user instanceof AppUser ?
                String.format(mContext.getString(R.string.parser_appuser_nick_changed),
                        oldNick, user.getColorfulNick()) :
                String.format(mContext.getString(R.string.parser_other_user_nick_change),
                        oldNick, user.getColorfulNick());

        if (channels != null) {
            for (final Channel channel : channels) {
                mSender.sendGenericChannelEvent(mServer, channel, message, false);
                channel.getUsers().update(user);
            }
        }
    }

    private void parseModeChange(final ArrayList<String> parsedArray, final String rawSource) {
        final String sendingUser = IRCUtils.getNickFromRaw(rawSource);
        final String recipient = parsedArray.get(2);
        final String mode = parsedArray.get(3);
        if (MiscUtils.isChannel(recipient.charAt(0))) {
            // The recipient is a channel (i.e. the mode of a user in the channel is being changed
            // or possibly the mode of the channel itself)
            if (parsedArray.size() == 4) {
                // User not specified - therefore channel mode is being changed
                // TODO - implement this?
            } else if (parsedArray.size() == 5) {
                // User specified - therefore user mode in channel is being changed
                final Channel channel = mUserChannelInterface.getChannel(recipient);
                final String userRecipient = parsedArray.get(4);
                final ChannelUser user = mUserChannelInterface.getUserFromRaw(userRecipient);

                final String message = user.processModeChange(mContext, sendingUser, channel,
                        mode);

                mSender.sendGenericChannelEvent(mServer, channel, message, true);
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
            mSender.sendGenericChannelEvent(mServer, channel,
                    String.format(mContext
                            .getString(R.string.parser_joined_channel), user.getPrettyNick(channel)), true);
        }
    }

    private void parseChannelPart(final ArrayList<String> parsedArray, final String rawSource,
                                  final boolean disconnectSent) {
        final String channelName = parsedArray.get(2);

        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
        final Channel channel = mUserChannelInterface.getChannel(channelName);
        if (user.equals(mServer.getUser())) {
            // This is a caveat of ZNC where it decides weirdly to tell the client to part all
            // the channels before closing to socket - don't do that
            if (!disconnectSent) {
                mSender.sendChanelParted(channel.getName());
            }
            mUserChannelInterface.removeChannel(channel);
        } else {
            String message = String.format(mContext.getString(R.string.parser_parted_channel),
                    user.getPrettyNick(channel));
            // If you have 4 strings in the array, the last must be the reason for parting
            message += (parsedArray.size() == 4) ? " " +
                    String.format(mContext.getString(R.string.parser_reason),
                            parsedArray.get(3).replace("\"", "")) : "";

            mSender.sendGenericChannelEvent(mServer, channel, message, true);
            mUserChannelInterface.decoupleUserAndChannel(user, channel);
        }
    }

    private boolean parseServerQuit(final ArrayList<String> parsedArray, final String rawSource) {
        final ChannelUser user = mUserChannelInterface.getUserFromRaw(rawSource);
        if (user.equals(mServer.getUser())) {
            // TODO - improve this
            return true;
        } else {
            final Set<Channel> list = mUserChannelInterface.removeUser(user);
            if (list != null) {
                for (final Channel channel : list) {
                    final String message = String.format(mContext.getString(R.string
                            .parser_quit_server),
                            user.getPrettyNick(channel)) +
                            // If you have 3 strings in the array, the last must be the reason for
                            // quitting
                            ((parsedArray.size() == 3) ? " " +
                                    String.format(mContext.getString(R.string.parser_reason),
                                            StringUtils.remove(parsedArray.get(2), "\"")) : "");
                    mSender.sendGenericChannelEvent(mServer, channel, message, true);
                }
            }
            return false;
        }
    }
}