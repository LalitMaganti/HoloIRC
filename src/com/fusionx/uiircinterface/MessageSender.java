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

package com.fusionx.uiircinterface;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.fusionx.Utils;
import com.fusionx.irc.Channel;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.Constants;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.irc.handlerabstract.ChannelHandler;
import com.fusionx.irc.handlerabstract.ServerHandler;
import com.fusionx.irc.handlerabstract.UserHandler;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.handlerabstract.ChannelFragmentHandler;
import com.fusionx.lightirc.handlerabstract.PMFragmentHandler;
import com.fusionx.lightirc.handlerabstract.ServerChannelHandler;
import com.fusionx.lightirc.handlerabstract.ServerFragHandler;

import java.util.LinkedHashMap;

public class MessageSender {
    private static LinkedHashMap<String, MessageSender> sender = new LinkedHashMap<>();
    private Context mContext;

    private Handler mServerHandler;
    private LinkedHashMap<String, Handler> mChannelHandlers = new LinkedHashMap<>();
    private LinkedHashMap<String, Handler> mPMHandlers = new LinkedHashMap<>();

    private Handler mServerFragmentHandler;
    private Handler mServerChannelHandler;
    private LinkedHashMap<String, Handler> mChannelFragmentHandlers = new LinkedHashMap<>();
    private LinkedHashMap<String, Handler> mPMFragmentHandlers = new LinkedHashMap<>();

    private MessageSender() {
    }

    public void initialSetup(Context context) {
        mContext = context;
    }

    public static MessageSender getSender(final String serverName) {
        MessageSender handler = sender.get(serverName);
        if (handler == null) {
            handler = new MessageSender();
            sender.put(serverName, handler);
        }
        return handler;
    }

    /*
    Start of registers
     */
    public void registerServerHandler(final ServerHandler serverHandler) {
        mServerHandler = serverHandler;
    }

    public void registerChannelHandler(final String channelName, final ChannelHandler channelHandler) {
        mChannelHandlers.put(channelName, channelHandler);
    }

    public void registerUserHandler(final String userNick, final UserHandler userHandler) {
        mPMHandlers.put(userNick, userHandler);
    }

    public void registerServerChannelHandler(final ServerChannelHandler serverChannelHandler) {
        mServerChannelHandler = serverChannelHandler;
    }

    public void registerServerFragmentHandler(final ServerFragHandler serverHandler) {
        mServerFragmentHandler = serverHandler;
    }

    public void registerChannelFragmentHandler(final String channelName,
                                               final ChannelFragmentHandler channelHandler) {
        mChannelFragmentHandlers.put(channelName, channelHandler);
    }

    public void registerUserFragmentHandler(final String userNick, final PMFragmentHandler userHandler) {
        mPMFragmentHandlers.put(userNick, userHandler);
    }

        /*
    Start of deregister
     */

    public void unregisterServerHandler() {
        mServerHandler = null;
    }

    public void unregisterChannelHandler(final String channelName) {
        mChannelHandlers.remove(channelName);
    }

    public void unregisterUserHandler(final String userNick) {
        mChannelHandlers.remove(userNick);
    }

    public void unregisterServerChannelHandler() {
        mServerChannelHandler = null;
    }

    public void unregisterServerFragmentHandler() {
        mServerFragmentHandler = null;
    }

    public void unregisterChannelFragmentHandler(final String channelName) {
        mChannelFragmentHandlers.remove(channelName);
    }

    public void unregisterUserFragmentHandler(final String userNick) {
        mPMFragmentHandlers.remove(userNick);
    }

    /*
    Start of sending messages
     */
    public void sendServerChannelMessage(final Bundle event) {
        if (mServerChannelHandler != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            mServerChannelHandler.sendMessage(fragmentMessage);
        }
    }

    public void sendServerMessage(final Bundle event) {
        final Message message = Message.obtain();
        message.setData(event);
        mServerHandler.dispatchMessage(message);

        if (mServerFragmentHandler != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            mServerFragmentHandler.sendMessage(fragmentMessage);
        }
    }

    private void sendChannelMessage(final Bundle event) {
        final String destination = event.getString(EventBundleKeys.destination);

        final Message message = Message.obtain();
        message.setData(event);
        final Handler handler = mChannelHandlers.get(destination);
        handler.dispatchMessage(message);

        if (mChannelFragmentHandlers.get(destination) != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            final Handler fragmentHandler = mChannelFragmentHandlers.get(destination);
            fragmentHandler.sendMessage(fragmentMessage);
        }
    }

    private void sendUserMessage(final Bundle event) {
        final String destination = event.getString(EventBundleKeys.destination);

        final Message message = Message.obtain();
        message.setData(event);
        final Handler handler = mPMHandlers.get(destination);
        handler.dispatchMessage(message);

        if (mPMFragmentHandlers.get(destination) != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            final Handler fragmentHandler = mPMFragmentHandlers.get(destination);
            fragmentHandler.sendMessage(fragmentMessage);
        }
    }

    /*
    End of internal methods
     */

    // Generic events start
    public void sendGenericServerEvent(final String message) {
        final Bundle joinEvent = Utils.parcelDataForBroadcast(null,
                ServerEventType.Generic, message);
        sendServerMessage(joinEvent);
    }

    public void sendGenericChannelEvent(final String channelName, final String message) {
        final Bundle privateMessageEvent = Utils.parcelDataForBroadcast(channelName,
                ChannelEventType.Generic, message);
        sendChannelMessage(privateMessageEvent);
    }

    public void sendGenericUserListChangedEvent(final String channelName, final String message) {
        final Bundle genericEvent = Utils.parcelDataForBroadcast(channelName,
                ChannelEventType.UserListChanged, message);
        sendChannelMessage(genericEvent);
    }

    private void sendGenericUserEvent(final String nick, final String message) {
        final Bundle privateMessageEvent = Utils.parcelDataForBroadcast(nick, UserEventType.Generic,
                message);
        sendUserMessage(privateMessageEvent);
    }
    // Generic events end

    public void sendServerConnection(final String connectionLine) {
        final Bundle joinEvent = Utils.parcelDataForBroadcast(null,
                ServerEventType.ServerConnected, connectionLine);
        sendServerMessage(joinEvent);
    }

    public void sendChanelJoined(final String channelName) {
        final Bundle joinEvent = Utils.parcelDataForBroadcast(null,
                ServerChannelEventType.Join, channelName);
        sendServerChannelMessage(joinEvent);
    }

    public void sendChanelParted(final String channelName) {
        final Bundle partEvent = Utils.parcelDataForBroadcast(channelName,
                ChannelEventType.UserParted, channelName);
        sendChannelMessage(partEvent);
    }

    public void sendAction(final String actionDestination, final User sendingUser,
                           final String rawAction) {
        String message;
        if (Constants.channelPrefixes.contains(actionDestination.charAt(0))) {
            message = String.format(mContext.getString(R.string.parser_action),
                    sendingUser.getPrettyNick(actionDestination), rawAction);
            sendGenericChannelEvent(actionDestination, message);
        } else {
            message = String.format(mContext.getString(R.string.parser_action),
                    sendingUser.getColorfulNick(), rawAction);
            sendGenericUserEvent(actionDestination, message);
        }
    }

    public void sendPrivateMessage(final User sending,
                                   final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message), sending.getColorfulNick(), rawMessage);
        sendGenericUserEvent(sending.getNick(), message);
    }

    public void sendMessageToChannel(final Channel channel, final User sending,
                                     final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getPrettyNick(channel), rawMessage);
        sendGenericChannelEvent(channel.getName(), message);
    }
}