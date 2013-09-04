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

package com.fusionx.lightirc.uiircinterface;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.IFragmentSideHandler;
import com.fusionx.lightirc.interfaces.IIRCSideHandler;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.User;
import com.fusionx.lightirc.constants.EventBundleKeys;
import com.fusionx.lightirc.constants.ChannelEventTypeEnum;
import com.fusionx.lightirc.constants.ServerChannelEventTypeEnum;
import com.fusionx.lightirc.constants.ServerEventTypeEnum;
import com.fusionx.lightirc.constants.UserEventTypeEnum;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.ui.IRCActivity;

import java.util.LinkedHashMap;

import lombok.NonNull;

public class MessageSender {
    private static LinkedHashMap<String, MessageSender> mHashMap = new LinkedHashMap<>();
    private Context mContext;
    private boolean mToast;

    private MessageSender() {
    }

    public void initialSetup(Context context) {
        mContext = context;
    }

    public static MessageSender getSender(final String serverName) {
        MessageSender handler = mHashMap.get(serverName);
        if (handler == null) {
            handler = new MessageSender();
            mHashMap.put(serverName, handler);
        }
        return handler;
    }

    private IIRCSideHandler ircSideHandlerInterface;
    private IFragmentSideHandler fragmentSideHandlerInterface;

    /*
    Start of registers
     */
    public void registerIRCSideHandlerInterface(final IIRCSideHandler handlerInterface) {
        ircSideHandlerInterface = handlerInterface;
    }

    public void registerServerChannelHandler(final IFragmentSideHandler handlerInterface) {
        fragmentSideHandlerInterface = handlerInterface;
    }

    /*
    Start of deregister
     */
    public void unregisterIRCSideHandlerInterface(final String serverName) {
        ircSideHandlerInterface = null;
        mHashMap.remove(serverName);
    }

    public void unregisterFragmentSideHandlerInterface() {
        fragmentSideHandlerInterface = null;
    }

    public void receiveMentionAsToast(final boolean toast) {
        mToast = toast;
    }

    /*
    Start of sending messages
     */
    private void sendServerChannelMessage(final Bundle event) {
        final Message message = Message.obtain();
        message.setData(event);
        ircSideHandlerInterface.getServerHandler().dispatchMessage(message);

        if (fragmentSideHandlerInterface != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            fragmentSideHandlerInterface.getServerChannelHandler().sendMessage(fragmentMessage);
        }
    }

    private void sendServerMessage(final Bundle event) {
        final Message message = Message.obtain();
        message.setData(event);
        ircSideHandlerInterface.getServerHandler().dispatchMessage(message);

        if (fragmentSideHandlerInterface != null) {
            final Handler handler = fragmentSideHandlerInterface.getFragmentHandler(null,
                    FragmentTypeEnum.Server);
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
            final Handler fragmentHandler = fragmentSideHandlerInterface.getFragmentHandler
                    (destination, FragmentTypeEnum.Channel);
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
            final Handler fragmentHandler = fragmentSideHandlerInterface.getFragmentHandler
                    (destination, FragmentTypeEnum.User);
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
        final Bundle joinEvent = parcelDataForBroadcast(null,
                ServerEventTypeEnum.Generic, message);
        sendServerMessage(joinEvent);
    }

    public void sendGenericChannelEvent(final String channelName, final String message) {
        final Bundle privateMessageEvent = parcelDataForBroadcast(channelName,
                ChannelEventTypeEnum.Generic, message);
        sendChannelMessage(privateMessageEvent);
    }

    public void sendGenericUserListChangedEvent(final String channelName, final String message) {
        final Bundle genericEvent = parcelDataForBroadcast(channelName,
                ChannelEventTypeEnum.UserListChanged, message);
        sendChannelMessage(genericEvent);
    }

    private void sendGenericUserEvent(final String nick, final String message) {
        final Bundle privateMessageEvent = parcelDataForBroadcast(nick, UserEventTypeEnum.Generic,
                message);
        sendUserMessage(privateMessageEvent);
    }
    // Generic events end

    public void sendServerConnection(final String connectionLine) {
        final Bundle connectEvent = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.Connected, connectionLine);
        sendServerChannelMessage(connectEvent);
    }

    public void sendFinalDisconnection(final String disconnectLine,
                                       final boolean expectedDisconnect) {
        final Bundle disconnectEvent = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.FinalDisconnected, disconnectLine);
        disconnectEvent.putBoolean(EventBundleKeys.disconnectSentByUser, expectedDisconnect);
        sendServerChannelMessage(disconnectEvent);
    }

    public void sendRetryPendingServerDisconnection(final String disconnectLine) {
        final Bundle disconnectEvent = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.RetryPendingDisconnected, disconnectLine);
        sendServerChannelMessage(disconnectEvent);
    }

    public void sendChanelJoined(final String channelName) {
        final Bundle joinEvent = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.Join, channelName);
        sendServerChannelMessage(joinEvent);
    }

    public void sendChanelParted(final String channelName) {
        final Bundle partEvent = parcelDataForBroadcast(channelName,
                ChannelEventTypeEnum.UserParted, channelName);
        sendChannelMessage(partEvent);
    }

    public void sendPrivateAction(final String actionDestination, final User sendingUser,
                                  final String rawAction) {
        String message = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getColorfulNick(), rawAction);
        // TODO - change this to be specific for PMs
        mention(actionDestination);
        sendGenericUserEvent(actionDestination, message);
    }

    public void sendChannelAction(final String actionDestination, final ChannelUser sendingUser,
                                  final String rawAction) {
        String finalMessage = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getPrettyNick(actionDestination), rawAction);
        if (rawAction.toLowerCase().contains(ircSideHandlerInterface.getNick().toLowerCase())) {
            mention(actionDestination);
            finalMessage = "<b>" + finalMessage + "</b>";
        }
        sendGenericChannelEvent(actionDestination, finalMessage);
    }

    /**
     * Method used to send a private message.
     * <p/>
     * Method should not be used from anywhere but the Server class.
     *
     * @param messageDestination - the nick name of the destination user
     * @param sending            - the user who is sending the message - it may be us or it may
     *                           be the other user
     * @param rawMessage         - the message being sent
     */
    public void sendPrivateMessage(final String messageDestination, final User sending,
                                   final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getColorfulNick(), rawMessage);
        // TODO - change this to be specific for PMs
        mention(messageDestination);
        sendGenericUserEvent(messageDestination, message);
    }

    public void sendMessageToChannel(final Channel channel, final ChannelUser sending,
                                     final String rawMessage) {
        String preMessage = String.format(mContext.getString(R.string.parser_message),
                sending.getPrettyNick(channel), rawMessage);
        if (rawMessage.toLowerCase().contains(ircSideHandlerInterface.getNick().toLowerCase())) {
            mention(channel.getName());
            preMessage = "<b>" + preMessage + "</b>";
        }
        sendGenericChannelEvent(channel.getName(), preMessage);
    }

    public void sendNickInUseMessage() {
        final Bundle event = parcelDataForBroadcast(null,
                ServerEventTypeEnum.NickInUse, mContext.getString(R.string.parser_nick_in_use));
        sendServerMessage(event);
    }

    public void switchToServerMessage(final String message) {
        final Bundle event = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.SwitchToServerMessage, message);
        sendServerChannelMessage(event);
    }

    public void userListReceived(final String channelName) {
        final Bundle event = parcelDataForBroadcast(channelName,
                ChannelEventTypeEnum.UserListReceived);
        sendChannelMessage(event);
    }

    public void sendNewPrivateMessage(final String nick) {
        final Bundle event = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.NewPrivateMessage, nick);
        sendServerChannelMessage(event);
    }

    public void setConnected(final String url) {
        final Bundle event = parcelDataForBroadcast(null,
                ServerChannelEventTypeEnum.Connected, String.format(mContext
                .getString(R.string.parser_connected), url));
        sendServerChannelMessage(event);
    }

    public Bundle parcelDataForBroadcast(final String destination,
                                         @NonNull final Enum type,
                                         @NonNull final String... message) {
        final Bundle event = new Bundle();
        if (destination != null) {
            event.putString(EventBundleKeys.destination, destination);
        }
        event.putSerializable(EventBundleKeys.eventType, type);
        if (message.length > 0) {
            event.putString(EventBundleKeys.message, message[0]);
        }

        return event;
    }

    public void mention(final String messageDestination) {
        if (mToast && fragmentSideHandlerInterface != null) {
            final Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    fragmentSideHandlerInterface.onMention(messageDestination);
                }
            });
        } else {
            final NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setContentTitle(mContext.getString(R.string.app_name))
                    .setContentText(mContext.getString(R.string.service_you_mentioned) + " " +
                            messageDestination)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true)
                    .setTicker(mContext.getString(R.string.service_you_mentioned) + " " +
                            messageDestination);
            final Intent mIntent = new Intent(mContext, IRCActivity.class);
            mIntent.putExtra("serverTitle", ircSideHandlerInterface.getTitle());
            mIntent.putExtra("mention", messageDestination);
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
            taskStackBuilder.addParentStack(IRCActivity.class);
            taskStackBuilder.addNextIntent(mIntent);
            final PendingIntent pIntent = taskStackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationManager.notify(345, builder.setContentIntent(pIntent).build());
        }
    }
}