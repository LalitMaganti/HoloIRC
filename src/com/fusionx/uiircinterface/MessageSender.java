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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.fusionx.common.Utils;
import com.fusionx.irc.Channel;
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.IRCFragmentActivity;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.uiircinterface.interfaces.FragmentSideHandlerInterface;
import com.fusionx.uiircinterface.interfaces.IRCSideHandlerInterface;

import java.util.LinkedHashMap;

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
    public void sendServerChannelMessage(final Bundle event) {
        final Message message = Message.obtain();
        message.setData(event);
        ircSideHandlerInterface.getServerHandler().dispatchMessage(message);

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
            final Handler handler = fragmentSideHandlerInterface.getFragmentHandler(null,
                    FragmentType.Server);
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
                    (destination, FragmentType.Channel);
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
                    (destination, FragmentType.User);
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
                ServerChannelEventType.Connected, connectionLine);
        sendServerChannelMessage(connectEvent);
    }

    public void sendFinalDisconnection(final String disconnectLine,
                                       final boolean expectedDisconnect) {
        final Bundle disconnectEvent = Utils.parcelDataForBroadcast(null,
                ServerChannelEventType.FinalDisconnected, disconnectLine);
        disconnectEvent.putBoolean(EventBundleKeys.disconnectSentByUser, expectedDisconnect);
        sendServerChannelMessage(disconnectEvent);
    }

    public void sendRetryPendingServerDisconnection(final String disconnectLine) {
        final Bundle disconnectEvent = Utils.parcelDataForBroadcast(null,
                ServerChannelEventType.RetryPendingDisconnected, disconnectLine);
        sendServerChannelMessage(disconnectEvent);
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
        String message = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getColorfulNick(), rawAction);
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
     * @param sending            - the user who is sending the message - it may be us or it may be the other
     *                           user
     * @param rawMessage         - the message being sent
     */
    public void sendPrivateMessage(final String messageDestination, final User sending,
                                   final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getColorfulNick(), rawMessage);
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

    public void switchToServerMessage(final String message) {
        final Bundle event = Utils.parcelDataForBroadcast(null,
                ServerChannelEventType.SwitchToServerMessage, message);
        sendServerChannelMessage(event);
    }

    public void userListReceived(final String channelName) {
        final Bundle event = Utils.parcelDataForBroadcast(channelName,
                ChannelEventType.UserListReceived);
        sendChannelMessage(event);
    }

    public void mention(final String messageDestination) {
        final NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.service_you_mentioned) + " " +
                        messageDestination)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setTicker(mContext.getString(R.string.service_you_mentioned) + " " +
                        messageDestination);

        if (mToast && fragmentSideHandlerInterface != null) {
            final Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    fragmentSideHandlerInterface.onMention(messageDestination);
                }
            });
        } else {
            final Intent mIntent = new Intent(mContext, IRCFragmentActivity.class);
            mIntent.putExtra("serverTitle", ircSideHandlerInterface.getTitle());
            mIntent.putExtra("mention", messageDestination);
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
            taskStackBuilder.addParentStack(IRCFragmentActivity.class);
            taskStackBuilder.addNextIntent(mIntent);
            final PendingIntent pIntent = taskStackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notification = builder.setContentIntent(pIntent).build();
            mNotificationManager.notify(345, notification);
        }
    }
}