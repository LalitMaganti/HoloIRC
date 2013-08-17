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

import android.os.Bundle;
import android.os.Message;

import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.handlerabstract.ServerHandler;
import com.fusionx.irc.misc.Utils;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Server {
    protected ServerWriter writer;
    protected UserChannelInterface userChannelInterface;

    protected final String title;
    protected AppUser user;

    @Setter(AccessLevel.NONE)
    protected String buffer = "";
    protected String status = "Disconnected";
    protected String MOTD = "";

    public Server(final String serverTitle) {
        title = serverTitle;

        MessageSender.getSender(serverTitle).registerServerHandler(serverHandler);
    }

    public void privateMessageSent(final User sendingUser, final String message) {
        final MessageSender sender = MessageSender.getSender(title);
        if (!user.isPrivateMessageOpen(sendingUser)) {
            user.newPrivateMessage(sendingUser);

            if(StringUtils.isNotEmpty(message)) {
                sender.sendPrivateMessage(sendingUser, message);
            }

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerChannelEventType.NewPrivateMessage, sendingUser.getNick());
            sender.sendServerChannelMessage(event);
        } else {
            sender.sendPrivateMessage(sendingUser, message);
        }
    }

    public void privateActionSent(final User sendingUser, final String action) {
        final MessageSender sender = MessageSender.getSender(title);
        if (!user.isPrivateMessageOpen(sendingUser)) {
            user.newPrivateMessage(sendingUser);

            sender.sendAction(sendingUser.getNick(), sendingUser, action);

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerChannelEventType.NewPrivateMessage, sendingUser.getNick());
            sender.sendServerMessage(event);
        } else {
            sender.sendAction(sendingUser.getNick(), sendingUser, action);
        }
    }

    private ServerHandler serverHandler = new ServerHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerEventType type = (ServerEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            switch (type) {
                case ServerConnected:
                case Generic:
                case Error:
                    buffer += bundle.getString(EventBundleKeys.message) + "\n";
                    break;
            }
        }
    };
}