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
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.handlerabstract.ServerFragHandler;
import com.fusionx.uiircinterface.interfaces.FragmentSideHandlerInterface;
import com.fusionx.uiircinterface.interfaces.IRCSideHandlerInterface;

import java.util.LinkedHashMap;

public class MessageSender {
    private static LinkedHashMap<String, MessageSender> sender = new LinkedHashMap<>();
    private Context mContext;

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

    private IRCSideHandlerInterface ircSideHandlerInterface;
    private FragmentSideHandlerInterface fragmentSideHandlerInterface;

    /*
    Start of registers
     */
    public void registerIRCSideHandlerInterface(final IRCSideHandlerInterface handlerInterface) {
        ircSideHandlerInterface = handlerInterface;
    }

    public void registerServerChannelHandler(final FragmentSideHandlerInterface handlerInterface) {
        fragmentSideHandlerInterface = handlerInterface;
    }

    /*
    Start of deregister
     */
    public void unregisterIRCSideHandlerInterface(final String serverName) {
        ircSideHandlerInterface = null;
        sender.remove(serverName);
    }

    public void unregisterFragmentSideHandlerInterface() {
        fragmentSideHandlerInterface = null;
    }

    /*
    Start of sending messages
     */
    public void sendServerChannelMessage(final Bundle event) {
        if (fragmentSideHandlerInterface != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            fragmentSideHandlerInterface.getServerChannelHandler().sendMessage(fragmentMessage);
        }
    }

    public void sendServerMessage(final Bundle event) {
        final Message message = Message.obtain();
        message.setData(event);
        ircSideHandlerInterface.getServerHandler().dispatchMessage(message);

        if (fragmentSideHandlerInterface != null) {
            final ServerFragHandler handler = fragmentSideHandlerInterface
                    .getServerFragmentHandler();
            if (handler != null) {
                final Message fragmentMessage = Message.obtain();
                fragmentMessage.setData(event);
                handler.sendMessage(fragmentMessage);
            }
        }
    }

    private void sendChannelMessage(final Bundle event) {
        final String destination = event.getString(EventBundleKeys.destination);

        final Message message = Message.obtain();
        message.setData(event);
        final Handler handler = ircSideHandlerInterface.getChannelHandler(destination);
        handler.dispatchMessage(message);

        if (fragmentSideHandlerInterface != null) {
            final Handler fragmentHandler = fragmentSideHandlerInterface
                    .getChannelFragmentHandler(destination);
            if (fragmentHandler != null) {
                final Message fragmentMessage = Message.obtain();
                fragmentMessage.setData(event);
                fragmentHandler.sendMessage(fragmentMessage);
            }
        }
    }

    private void sendUserMessage(final Bundle event) {
        final String destination = event.getString(EventBundleKeys.destination);

        final Message message = Message.obtain();
        message.setData(event);
        final Handler handler = ircSideHandlerInterface.getUserHandler(destination);
        handler.dispatchMessage(message);

        if (fragmentSideHandlerInterface != null) {
            final Handler fragmentHandler = fragmentSideHandlerInterface.getUserFragmentHandler(destination);
            if (fragmentHandler != null) {
                final Message fragmentMessage = Message.obtain();
                fragmentMessage.setData(event);
                fragmentHandler.sendMessage(fragmentMessage);
            }
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
        final Bundle connectEvent = Utils.parcelDataForBroadcast(null,
                ServerEventType.Connected, connectionLine);
        sendServerMessage(connectEvent);
    }

    public void sendServerDisconnection(final String disconnectLine) {
        final Bundle disconnectEvent = Utils.parcelDataForBroadcast(null,
                ServerEventType.Disconnected, disconnectLine);
        sendServerMessage(disconnectEvent);
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

    public void sendPrivateAction(final String actionDestination, final User sendingUser,
                                  final String rawAction) {
        String message;

        message = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getColorfulNick(), rawAction);
        sendGenericUserEvent(actionDestination, message);
    }

    public void sendChannelAction(final String actionDestination, final ChannelUser sendingUser,
                                  final String rawAction) {
        String message;
        message = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getPrettyNick(actionDestination), rawAction);
        sendGenericChannelEvent(actionDestination, message);
    }

    /**
     * Method used to send a private message.
     * <p/>
     * Method should not be used from anywhere but the Server class.
     *
     * @param messageDestination - the nick name of the destination user
     * @param sending            - the user who is sending the message - it may be us or it may be the other
     *                           user
     * @param rawMessage         - the message being sent
     */
    public void sendPrivateMessage(final String messageDestination, final User sending,
                                   final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getColorfulNick(), rawMessage);
        sendGenericUserEvent(messageDestination, message);
    }

    public void sendMessageToChannel(final Channel channel, final ChannelUser sending,
                                     final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getPrettyNick(channel), rawMessage);
        sendGenericChannelEvent(channel.getName(), message);
    }
}