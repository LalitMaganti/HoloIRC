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

import com.fusionx.lightirc.irc.IOExceptionEvent;
import com.fusionx.lightirc.irc.IrcExceptionEvent;
import com.fusionx.lightirc.misc.UserComparator;
import com.fusionx.lightirc.parser.EventParser;
import com.fusionx.lightirc.service.IRCService;
import lombok.*;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.PrivateActionEvent;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

import java.util.ArrayList;
import java.util.Collections;

@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceListener extends GenericListener {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PUBLIC)
    private IRCService service;

    @Override
    public void onConnect(final ConnectEvent event) {
        event.getBot().setStatus("Connected");
    }

    @Override
    public void onDisconnect(final DisconnectEvent event) {
        event.getBot().setStatus("Disconnected");
        service.getBotManager().remove(event.getBot());
        if (service.getBotManager().keySet().isEmpty()) {
            service.stopForeground(true);
            service.stopSelf();
        }
    }

    // Server stuff
    @Override
    public void onEvent(final Event event) throws Exception {
        super.onEvent(event);

        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            event.getBot().appendToBuffer(EventParser.getOutputForEvent(event));
        }
    }

    @Override
    protected void onIOException(IOExceptionEvent<PircBotX> event) {
        event.getBot().appendToBuffer(EventParser.getOutputForEvent(event));
    }

    @Override
    protected void onIrcException(IrcExceptionEvent event) {
        event.getBot().appendToBuffer(event.getException().getMessage());
    }

    // Channel stuff
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));
    }

    @Override
    public void onTopic(final TopicEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));
    }

    @Override
    public void onMessage(final MessageEvent<PircBotX> event) {
        if (event.getMessage().contains(event.getBot().getNick())) {
            final String title = event.getBot().getConfiguration().getTitle();
            getService().mention(title, event.getChannel().getName());
        }

        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));
    }

    @Override
    public void onUserList(final UserListEvent<PircBotX> event) {
        final ArrayList<String> userList = new ArrayList<String>();

        if (userList.isEmpty()) {
            for (final User u : event.getUsers()) {
                userList.add(u.getPrettyNick(event.getChannel()));
            }

            event.getChannel().initialUserList(userList);
            Collections.sort(userList, new UserComparator());
        }
    }

    @Override
    public void onAction(final ActionEvent<PircBotX> event) {
        if (event.getMessage().contains(event.getBot().getNick())) {
            final String title = event.getBot().getConfiguration().getTitle();
            getService().mention(title, event.getChannel().getName());
        }

        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        final ArrayList<String> set = event.getChannel().getUserList();
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));

        set.set(set.indexOf(oldFormattedNick), newFormattedNick);
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void onOtherUserJoin(final JoinEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.add(event.getUser().getPrettyNick(event.getChannel()));
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void onOtherUserPart(final PartEvent<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.remove(event.getUser().getPrettyNick(event.getChannel()));
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event) {
        event.getChannel().appendToBuffer(EventParser.getOutputForEvent(event));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.remove(event.getUser().getPrettyNick(event.getChannel()));
        Collections.sort(set, new UserComparator());
    }

    // Private message stuff
    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        if (!event.getMessage().equals("")) {
            event.getUser().appendToBuffer(EventParser.getOutputForEvent(event));
        }

        if (!event.getUser().equals(event.getBot().getUserBot())) {
            final String title = event.getBot().getConfiguration().getTitle();
            getService().mention(title, event.getUser().getNick());
        }
    }

    @Override
    public void onPrivateAction(final PrivateActionEvent<PircBotX> event) {
        event.getUser().appendToBuffer(EventParser.getOutputForEvent(event));

        if (!event.getUser().equals(event.getBot().getUserBot())) {
            final String title = event.getBot().getConfiguration().getTitle();
            getService().mention(title, event.getUser().getNick());
        }
    }
}