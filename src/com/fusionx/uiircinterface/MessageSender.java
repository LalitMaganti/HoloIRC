package com.fusionx.uiircinterface;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fusionx.irc.Channel;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.Constants;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.irc.misc.Utils;
import com.fusionx.lightirc.R;

import java.util.LinkedHashMap;

import static com.fusionx.irc.constants.Constants.LOG_TAG;

public class MessageSender {
    private static LinkedHashMap<String, MessageSender> sender = new LinkedHashMap<>();
    private Context mContext;

    private Handler mServerHandler;
    private LinkedHashMap<String, Handler> mChannelHandlers = new LinkedHashMap<>();
    private LinkedHashMap<String, Handler> mPMHandlers = new LinkedHashMap<>();

    private Handler mServerFragmentHandler;
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
    public void registerServerHandler(final Handler serverHandler) {
        mServerHandler = serverHandler;
    }

    public void registerChannelHandler(final String channelName, final Handler channelHandler) {
        mChannelHandlers.put(channelName, channelHandler);
    }

    public void registerUserHandler(final String userNick, final Handler userHandler) {
        mPMHandlers.put(userNick, userHandler);
    }

    public void registerServerFragmentHandler(final Handler serverHandler) {
        mServerFragmentHandler = serverHandler;
    }

    public void registerChannelFragmentHandler(final String channelName,
                                               final Handler channelHandler) {
        mChannelFragmentHandlers.put(channelName, channelHandler);
    }

    public void registerUserFragmentHandler(final String userNick, final Handler userHandler) {
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

    public void unregisterServerFragmentHandler() {
        mServerFragmentHandler = null;
        Log.e(LOG_TAG, "Server fragment unregistered");
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
    public void sendServerMessage(final Bundle event) {
        final Message message = Message.obtain();
        message.setData(event);
        mServerHandler.dispatchMessage(message);

        Log.e(LOG_TAG, "Sent server message to service");
        if (mServerFragmentHandler != null) {
            final Message fragmentMessage = Message.obtain();
            fragmentMessage.setData(event);
            mServerFragmentHandler.sendMessage(fragmentMessage);

            Log.e(LOG_TAG, "Sent server message to front end");
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

    public void sendUserMessage(final Bundle event) {
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

    public void sendGenericUserEvent(final String nick, final String message) {
        final Bundle privateMessageEvent = Utils.parcelDataForBroadcast(nick, UserEventType.Generic,
                message);
        sendUserMessage(privateMessageEvent);
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
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getColorfulNick(), rawMessage);
        sendGenericUserEvent(sending.getNick(), message);
    }

    public void sendServerConnection(final String connectionLine) {
        final Bundle joinEvent = Utils.parcelDataForBroadcast(null,
                ServerEventType.ServerConnected, connectionLine);
        sendServerMessage(joinEvent);
    }

    public void sendGenericUserListChangedEvent(final String channelName, final String message) {
        final Bundle genericEvent = Utils.parcelDataForBroadcast(channelName,
                ChannelEventType.UserListChanged, message);
        sendChannelMessage(genericEvent);
    }

    public void sendChanelJoined(final String channelName) {
        final Bundle joinEvent = Utils.parcelDataForBroadcast(null,
                ServerEventType.Join, channelName);
        sendServerMessage(joinEvent);
    }

    public void sendChanelParted(final String channelName) {
        final Bundle partEvent = Utils.parcelDataForBroadcast(channelName,
                ChannelEventType.UserParted, channelName);
        sendChannelMessage(partEvent);
    }

    public void sendUserMessageToChannel(final Channel channel, final User sending,
                                         final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getPrettyNick(channel), rawMessage);
        sendGenericChannelEvent(channel.getName(), message);
    }
}