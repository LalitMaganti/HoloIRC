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

package com.fusionx.irc;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.fusionx.common.Utils;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.handlerabstract.ChannelHandler;
import com.fusionx.irc.handlerabstract.ServerHandler;
import com.fusionx.irc.handlerabstract.UserHandler;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.lightirc.R;
import com.fusionx.uiircinterface.MessageSender;
import com.fusionx.uiircinterface.interfaces.IRCSideHandlerInterface;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

@Data
public class Server implements IRCSideHandlerInterface {
    private ServerWriter writer;
    private UserChannelInterface userChannelInterface;

    private final String title;
    private AppUser user;

    @Setter(AccessLevel.NONE)
    private String buffer = "";
    private String status = "Disconnected";
    private String MOTD = "";

    private final ServerHandler serverHandler = new ServerHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerEventType type = (ServerEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            switch (type) {
                case Disconnected:
                case Error:
                    MessageSender.getSender(title).unregisterIRCSideHandlerInterface(title);
                case NickInUse:
                case Connected:
                case Generic:
                    buffer += bundle.getString(EventBundleKeys.message) + "\n";
                    break;
            }
        }
    };

    public Server(final String serverTitle) {
        title = serverTitle;

        MessageSender.getSender(serverTitle).registerIRCSideHandlerInterface(this);
    }

    public void privateMessageSent(final PrivateMessageUser userWhoIsNotUs, final String message,
                                   final boolean weAreSending) {
        final MessageSender sender = MessageSender.getSender(title);
        final User sendingUser = weAreSending ? user : userWhoIsNotUs;
        if (!user.isPrivateMessageOpen(userWhoIsNotUs)) {
            user.createPrivateMessage(userWhoIsNotUs);

            if (StringUtils.isNotEmpty(message)) {
                sender.sendPrivateMessage(userWhoIsNotUs.getNick(), sendingUser, message);
            }

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerChannelEventType.NewPrivateMessage, userWhoIsNotUs.getNick());
            sender.sendServerChannelMessage(event);
        } else {
            if (StringUtils.isNotEmpty(message)) {
                sender.sendPrivateMessage(userWhoIsNotUs.getNick(), sendingUser, message);
            }
        }
    }

    public void privateActionSent(final PrivateMessageUser userWhoIsNotUs, final String action,
                                  final boolean weAreSending) {
        final MessageSender sender = MessageSender.getSender(title);
        final User sendingUser = weAreSending ? user : userWhoIsNotUs;
        if (!user.isPrivateMessageOpen(userWhoIsNotUs)) {
            user.createPrivateMessage(userWhoIsNotUs);

            if (StringUtils.isNotEmpty(action)) {
                sender.sendPrivateAction(userWhoIsNotUs.getNick(), sendingUser, action);
            }

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerChannelEventType.NewPrivateMessage, userWhoIsNotUs.getNick());
            sender.sendServerMessage(event);
        } else {
            if (StringUtils.isNotEmpty(action)) {
                sender.sendPrivateAction(userWhoIsNotUs.getNick(), sendingUser, action);
            }
        }
    }

    public synchronized PrivateMessageUser getPrivateMessageUser(@NonNull final String nick) {
        final Iterator<PrivateMessageUser> iterator = user.getPrivateMessageIterator();
        while (iterator.hasNext()) {
            final PrivateMessageUser privateMessageUser = iterator.next();
            if (Utils.areNicksEqual(privateMessageUser.getNick(), nick)) {
                return privateMessageUser;
            }
        }
        return new PrivateMessageUser(nick, userChannelInterface);
    }

    @Override
    public ChannelHandler getChannelHandler(String channelName) {
        return userChannelInterface.getChannel(channelName).getChannelHandler();
    }

    @Override
    public UserHandler getUserHandler(String userNick) {
        return getPrivateMessageUser(userNick).getUserHandler();
    }

    @Override
    public String getNick() {
        return getUser().getNick();
    }

    public boolean isConnected(final Context context) {
        return status.equals(context.getString(R.string.status_connected));
    }
}