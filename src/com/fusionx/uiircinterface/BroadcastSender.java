/*package com.fusionx.ircinterface.misc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.fusionx.ircinterface.Channel;
import com.fusionx.ircinterface.User;
import com.fusionx.ircinterface.constants.Constants;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ChannelEventType;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.lightirc.R;
import lombok.Getter;

public class BroadcastSender {
    @Getter
    private final LocalBroadcastManager broadcastManager;
    private final Context mContext;

    public BroadcastSender(final LocalBroadcastManager manager, Context context) {
        broadcastManager = manager;
        mContext = context;
    }

    public void sendBroadcast(final Event event) {
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(event.getDestination());
        broadcastIntent.putExtra("event", event);
        broadcastManager.sendBroadcast(broadcastIntent);
    }

    public void broadcastServerConnection(final String connectionLine) {
        /*final Event joinEvent = Utils.parcelDataForBroadcast(EventDestination.Server, null,
                ServerEventType.ServerConnected, connectionLine);
        sendBroadcast(joinEvent);
    }

    public void broadcastAction(final String actionDestination, final User sendingUser,
                                final String rawAction) {
        String message;
        if (Constants.channelPrefixes.contains(actionDestination.charAt(0))) {
            message = String.format(mContext.getString(R.string.parser_action),
                    sendingUser.getPrettyNick(actionDestination), rawAction);
            broadcastGenericChannelEvent(actionDestination, message);
        } else {
            message = String.format(mContext.getString(R.string.parser_action),
                    sendingUser.getColorfulNick(), rawAction);
            broadcastGenericUserEvent(actionDestination, message);
        }
    }

    public void broadcastUserMessageToChannel(final Channel channel, final User sending,
                                              final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getPrettyNick(channel), rawMessage);
        broadcastGenericChannelEvent(channel.getName(), message);
    }

    public void broadcastPrivateMessage(final User sending,
                                        final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getColorfulNick(), rawMessage);
        broadcastGenericUserEvent(sending.getNick(), message);
    }

    public void broadcastGenericServerEvent(final String message) {
        final Event joinEvent = Utils.parcelDataForBroadcast(EventDestination.Server, null,
                ServerEventType.Generic, message);
        sendBroadcast(joinEvent);
    }

    public void broadcastChanelJoined(final String channelName) {
        final Event joinEvent = Utils.parcelDataForBroadcast(EventDestination.Server, null,
                ServerEventType.Join, channelName);
        sendBroadcast(joinEvent);
    }

    public void broadcastChanelParted(final String channelName) {
        final Event partEvent = Utils.parcelDataForBroadcast(EventDestination.Channel, channelName,
                ChannelEventType.UserParted);
        sendBroadcast(partEvent);
    }

    public void broadcastGenericUserListChangedEvent(final String channelName, final String message) {
        final Event genericEvent = Utils.parcelDataForBroadcast(EventDestination.Channel,
                channelName, ChannelEventType.UserListChanged, message);
        sendBroadcast(genericEvent);
    }

    public void broadcastGenericUserEvent(final String nick, final String message) {
        final Event privateMessageEvent = Utils.parcelDataForBroadcast(EventDestination.User,
                nick, ChannelEventType.Generic, message);
        sendBroadcast(privateMessageEvent);
    }

    public void broadcastGenericChannelEvent(final String channelName, final String message) {
        final Event privateMessageEvent = Utils.parcelDataForBroadcast(EventDestination.Channel,
                channelName, ChannelEventType.Generic, message);
        sendBroadcast(privateMessageEvent);
    }
}*/