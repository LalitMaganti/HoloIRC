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

import com.fusionx.lightirc.activity.IRCFragmentActivity;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.PMFragment;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.EventParser;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.IOExceptionEvent;
import org.pircbotx.hooks.events.lightirc.IrcExceptionEvent;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

public class ActivityListener extends GenericListener {
    @Getter(AccessLevel.PRIVATE)
    private final IRCFragmentActivity activity;

    public ActivityListener(final IRCFragmentActivity activity) {
        super(activity.getApplicationContext());

        this.activity = activity;
    }

    // Server stuff
    @Override
    public void onConnect(final ConnectEvent<PircBotX> event) {
        appendToServer(event);
        getActivity().closeAllSlidingMenus();
    }

    // This HAS to be an unexpected disconnect. If it isn't then there's something wrong.
    @Override
    public void onDisconnect(final DisconnectEvent<PircBotX> event) {
        appendToServer(event);
        getActivity().onUnexpectedDisconnect();
    }

    @Override
    protected void onIrcException(final IrcExceptionEvent<PircBotX> event) {
        appendToServer(event);
    }

    @Override
    protected void onIOException(final IOExceptionEvent<PircBotX> event) {
        appendToServer(event);
    }

    // Server events
    @Override
    public void onNotice(final NoticeEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            if (event.getUser().getBuffer().isEmpty()) {
                appendToServer(event);
            } else {
                onPrivateEvent(event.getUser(), event.getNotice(), event);
            }
        } else {
            onChannelMessage(event.getChannel().getName(), event);
        }
    }

    @Override
    public void onMotd(final MotdEvent<PircBotX> event) {
        appendToServer(event);
    }

    @Override
    public void onUnknown(final UnknownEvent<PircBotX> event) {
        getActivity().getViewPager().setCurrentItem(0, true);
        appendToServer(event);
    }

    // Channel events
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int position = getActivity().onNewChannelJoined(event.getChannel().getName());
                getActivity().getViewPager().setCurrentItem(position, true);
            }
        });
    }

    @Override
    public void onMessage(final MessageEvent<PircBotX> event) {
        onChannelMessage(event.getChannel().getName(), event);
    }

    @Override
    public void onAction(final ActionEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            onPrivateEvent(event.getUser(), event.getAction(), event);
        } else {
            onChannelMessage(event.getChannel().getName(), event);
        }
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        userListChanged(event, event.getChannel().getName());
    }

    @Override
    public void onOtherUserJoin(final JoinEvent<PircBotX> event) {
        userListChanged(event, event.getChannel().getName());
    }

    @Override
    public void onOtherUserPart(final PartEvent<PircBotX> event) {
        userListChanged(event, event.getChannel().getName());
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event) {
        userListChanged(event, event.getChannel().getName());
    }

    @Override
    public void onMode(final ModeEvent<PircBotX> event) {
        if (event.getUser() != null) {
            userListChanged(event, event.getChannel().getName());
        }
    }

    private void userListChanged(final Event<PircBotX> event, final String channelName) {
        if (Utils.isMessagesFromChannelShown(applicationContext)) {
            onChannelMessage(channelName, event);
        }
        if (checkChannelFragment(channelName)) {
            getActivity().closeAllSlidingMenus();
        }
    }

    @Override
    protected void onUserPart(final PartEvent<PircBotX> event) {
        final IRCPagerAdapter adapter = getActivity().getIrcPagerAdapter();
        final int index = adapter.getItemPosition(adapter.getFragment(event.getChannel().getName()));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().removeIRCFragment(index);
            }
        });
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        onPrivateEvent(event.getUser(), event.getMessage(), event);
    }

    // Misc stuff
    private void onPrivateEvent(final User user, final String message, final Event<PircBotX> event) {
        final IRCFragment fragment = privateMessageCheck(user.getNick());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fragment != null) {
                    if (!message.equals("")) {
                        final PMFragment pm = (PMFragment) fragment;
                        pm.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
                    }
                } else {
                    getActivity().onNewPrivateMessage(user.getNick());
                }
            }
        });
    }

    private void appendToServer(final Event<PircBotX> event) {
        final IRCFragment server = getActivity().getIrcPagerAdapter()
                .getFragment(event.getBot().getConfiguration().getTitle());
        if(server != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
                }
            });
        }
    }

    private void onChannelMessage(final String channelName, final Event<PircBotX> event) {
        final IRCFragment channel = getActivity().getIrcPagerAdapter().getFragment(channelName);
        if (channel != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    channel.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
                }
            });
        }
    }

    private boolean checkChannelFragment(final String keyName) {
        return getActivity().getCurrentItem().getTitle().equals(keyName);
    }

    private IRCFragment privateMessageCheck(final String userNick) {
        return getActivity().getIrcPagerAdapter().getFragment(userNick);
    }
}