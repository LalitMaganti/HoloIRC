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
/*
package com.fusionx.holoirc.listeners;

import com.fusionx.holoirc.R;
import com.fusionx.holoirc.irc.IRCUserComparator;
import com.fusionx.holoirc.parser.EventParser;
import com.fusionx.holoirc.service.IRCBridgeService;

import java.util.ArrayList;
import java.util.Collections;

public class ServiceListener extends GenericListener {
    private final IRCBridgeService mService;

    public ServiceListener(final IRCBridgeService service) {
        super(service.getApplicationContext());
        mService = service;
    }

    // Server stuff
    @Override
    public void onConnect(final ConnectEvent<PircBotX> event) {
        event.getServer().setStatus(mService.getString(R.string.status_connected));

        event.getServer().appendToBuffer(EventParser.getOutputForEvent(event, mService));
    }

    // This HAS to be an unexpected disconnect. If it isn't then there's something wrong.
    @Override
    public void onDisconnect(final DisconnectEvent<PircBotX> event) {
        event.getServer().setStatus(mService.getString(R.string.status_disconnected));

        event.getServer().appendToBuffer(EventParser.getOutputForEvent(event, mService));

        mService.onUnexpectedDisconnect(event.getServer().getConfiguration().getTitle());
    }

    @Override
    public void onNotice(final NoticeEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            if (event.getUser().getBuffer().isEmpty()) {
                onServerMessage(event);
            } else {
                onUserMessage(event, event.getUser());
            }
        } else {
            onChannelMessage(event, event.getChannel());
        }
    }

    @Override
    public void onIOException(final IOExceptionEvent<PircBotX> event) {
        event.getServer().setStatus(mService.getString(R.string.status_disconnected));

        event.getServer().appendToBuffer(EventParser.getOutputForEvent(event, mService));

        mService.onUnexpectedDisconnect(event.getServer().getConfiguration().getTitle());
    }

    // Channel stuff
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        onChannelMessage(event, event.getChannel());
    }

    @Override
    public void onMessage(final MessageEvent<PircBotX> event) {
        super.onMessage(event);

        if (event.getMessage().contains(event.getServer().getNick())) {
            final String title = event.getServer().getConfiguration().getTitle();
            mService.mention(title, event.getChannel().getName());
        }
    }

    @Override
    public void onUserList(final UserListEvent<PircBotX> event) {
        onSetupChannelUserList(event.getChannel());
    }

    @Override
    public void onAction(final ActionEvent<PircBotX> event) {
        super.onAction(event);

        if (event.getChannel() != null && event.getMessage().contains(event.getServer().getNick())) {
            final String title = event.getServer().getConfiguration().getTitle();
            mService.mention(title, event.getChannel().getName());
        }
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        onChannelMessage(event, event.getChannel());

        final ArrayList<String> set = event.getChannel().getUserList();
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        set.set(set.indexOf(oldFormattedNick), newFormattedNick);
        Collections.sort(set, new IRCUserComparator());
    }

    @Override
    public void onOtherUserJoin(final JoinEvent<PircBotX> event) {
        onChannelMessage(event, event.getChannel());

        final ArrayList<String> set = event.getChannel().getUserList();
        set.add(event.getUser().getPrettyNick(event.getChannel()));
        Collections.sort(set, new IRCUserComparator());
    }

    @Override
    public void onMode(final ModeEvent<PircBotX> event) {
        if (event.getUser() != null) {
            super.onMode(event);

            onSetupChannelUserList(event.getChannel());
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
        onChannelMessage(event, channel);

        final ArrayList<String> set = channel.getUserList();
        set.remove(user.getPrettyNick(channel));
        Collections.sort(set, new IRCUserComparator());
    }

    @Override
    void onPrivateEvent(final Event<PircBotX> event, final User user, final String message) {
        if (!message.equals("")) {
            onUserMessage(event, user);
        }

        if (!event.getServer().getUserChannelDao().getPrivateMessages().contains(user)) {
            user.createPrivateMessage();
        }

        if (!user.equals(event.getServer().getUserBot())) {
            final String title = event.getServer().getConfiguration().getTitle();
            mService.mention(title, user.getNick());
        }
    }

    private void onSetupChannelUserList(final Channel channel) {
        final ArrayList<String> userList = new ArrayList<>();

        for (final User user : channel.getUsers()) {
            userList.add(user.getPrettyNick(channel));
        }

        Collections.sort(userList, new IRCUserComparator());
        channel.setUserList(userList);
    }

    @Override
    public void onServerMessage(Event<PircBotX> event) {
        event.getServer().appendToBuffer(EventParser.getOutputForEvent(event, mService));
    }

    @Override
    void onChannelMessage(Event<PircBotX> event, Channel channel) {
        channel.appendToBuffer(EventParser.getOutputForEvent(event, mService));
    }

    @Override
    void onUserMessage(Event<PircBotX> event, User user) {
        user.appendToBuffer(EventParser.getOutputForEvent(event, mService));
    }
}*/