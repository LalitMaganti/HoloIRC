/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.IRCUserComparator;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.EventParser;
import com.fusionx.lightirc.service.IRCService;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.IOExceptionEvent;
import org.pircbotx.hooks.events.lightirc.IrcExceptionEvent;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

import java.util.ArrayList;
import java.util.Collections;

public class ServiceListener extends GenericListener {
    @Getter(AccessLevel.PRIVATE)
    private final IRCService service;

    public ServiceListener(final IRCService service) {
        super(service.getApplicationContext());
        this.service = service;
    }

    // Server stuff
    @Override
    public void onConnect(final ConnectEvent<PircBotX> event) {
        event.getBot().setStatus(getService().getString(R.string.status_connected));

        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    // This HAS to be an unexpected disconnect. If it isn't then there's something wrong.
    @Override
    public void onDisconnect(final DisconnectEvent<PircBotX> event) {
        event.getBot().setStatus(getService().getString(R.string.status_disconnected));

        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));

        if (getService().getThreadManager().keySet().size() == 1) {
            getService().stopForeground(true);
        }

        getService().onUnexpectedDisconnect(event.getBot().getConfiguration().getTitle());
    }

    @Override
    public void onNotice(final NoticeEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            if (event.getUser().getBuffer().isEmpty()) {
                event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
            } else {
                event.getUser().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
            }
        } else {
            event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
        }
    }

    @Override
    public void onMotd(final MotdEvent<PircBotX> event) {
        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    @Override
    public void onIOException(IOExceptionEvent<PircBotX> event) {
        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    @Override
    public void onIrcException(IrcExceptionEvent<PircBotX> event) {
        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    @Override
    public void onUnknown(final UnknownEvent<PircBotX> event) {
        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    // Channel stuff
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    @Override
    public void onTopic(final TopicEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    @Override
    public void onMessage(final MessageEvent<PircBotX> event) {
        if (event.getMessage().contains(event.getBot().getNick())) {
            final String title = event.getBot().getConfiguration().getTitle();
            getService().mention(title, event.getChannel().getName());
        }

        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
    }

    @Override
    public void onUserList(final UserListEvent<PircBotX> event) {
        final ArrayList<String> userList = new ArrayList<String>();

        for (final User user : event.getUsers()) {
            userList.add(user.getPrettyNick(event.getChannel()));
        }

        Collections.sort(userList, new IRCUserComparator());
        event.getChannel().setUserList(userList);
    }

    @Override
    public void onAction(final ActionEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            onPrivateEvent(event, event.getAction(), event.getUser());
        } else {
            event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));

            if (event.getMessage().contains(event.getBot().getNick())) {
                final String title = event.getBot().getConfiguration().getTitle();
                getService().mention(title, event.getChannel().getName());
            }
        }
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        final ArrayList<String> set = event.getChannel().getUserList();
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));

        set.set(set.indexOf(oldFormattedNick), newFormattedNick);
        Collections.sort(set, new IRCUserComparator());
    }

    @Override
    public void onOtherUserJoin(final JoinEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.add(event.getUser().getPrettyNick(event.getChannel()));
        Collections.sort(set, new IRCUserComparator());
    }

    @Override
    public void onMode(final ModeEvent<PircBotX> event) {
        if (event.getUser() != null) {
            if (Utils.isMessagesFromChannelShown(applicationContext)) {
                event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event, getService()));
            }
            final ArrayList<String> userList = new ArrayList<String>();

            for (final User user : event.getChannel().getUsers()) {
                userList.add(user.getPrettyNick(event.getChannel()));
            }

            event.getChannel().setUserList(userList);
            Collections.sort(userList, new IRCUserComparator());
        }
    }

    @Override
    public void onOtherUserPart(final PartEvent<PircBotX> event) {
        onOtherUserDepart(event, event.getChannel(), event.getUser());
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event) {
        onOtherUserDepart(event, event.getChannel(), event.getUser());
    }

    private void onOtherUserDepart(final Event<PircBotX> event, final Channel channel, final User user) {
        channel.appendToBuffer(EventParser.getOutputForEvent(event, getService()));

        final ArrayList<String> set = channel.getUserList();
        set.remove(user.getPrettyNick(channel));
        Collections.sort(set, new IRCUserComparator());
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        onPrivateEvent(event, event.getMessage(), event.getUser());
    }

    public void onPrivateEvent(final Event<PircBotX> event, final String message, final User user) {
        if (!message.equals("")) {
            user.appendToBuffer(EventParser.getOutputForEvent(event, getService()));
        }

        if (!user.equals(event.getBot().getUserBot())) {
            final String title = event.getBot().getConfiguration().getTitle();
            getService().mention(title, user.getNick());
        }
    }
}