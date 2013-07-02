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

import android.support.v4.view.ViewPager;
import com.fusionx.lightirc.activity.IRCFragmentActivity;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.PMFragment;
import com.fusionx.lightirc.irc.IOExceptionEvent;
import com.fusionx.lightirc.irc.IrcExceptionEvent;
import com.fusionx.lightirc.misc.UserComparator;
import com.fusionx.lightirc.parser.EventParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

import java.util.ArrayList;
import java.util.Collections;

public class ActivityListener extends GenericListener {
    @Getter(AccessLevel.PRIVATE)
    private final IRCFragmentActivity activity;
    private final IRCPagerAdapter mIRCPagerAdapter;
    private final ViewPager mViewPager;

    @Setter(AccessLevel.PUBLIC)
    private UserListAdapter arrayAdapter;

    public ActivityListener(final IRCFragmentActivity activity, final IRCPagerAdapter adapter, final ViewPager pager) {
        this.activity = activity;
        mIRCPagerAdapter = adapter;
        mViewPager = pager;
    }

    @Override
    protected void onIrcException(final IrcExceptionEvent event) {
        final IRCFragment server = (IRCFragment) mIRCPagerAdapter.getItem(0);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                server.appendToTextView(event.getException().getMessage());
            }
        });
    }

    @Override
    protected void onIOException(final IOExceptionEvent<PircBotX> event) {
        final IRCFragment server = (IRCFragment) mIRCPagerAdapter.getItem(0);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                server.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
            }
        });
    }

    // Server events
    @Override
    public void onEvent(final Event event) throws Exception {
        super.onEvent(event);

        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            final IRCFragment server = (IRCFragment) mIRCPagerAdapter.getItem(0);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
                }
            });
        }
    }

    // Channel events
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        final JoinEvent joinevent = (JoinEvent) event;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().onNewChannelJoined(joinevent.getChannel().getName(), null);
            }
        });
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

        final String channelName = event.getChannel().getName();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ChannelFragment channel = (ChannelFragment) mIRCPagerAdapter
                        .getTab(channelName);
                if (channel != null) {
                    channel.setUserList(userList);
                }
            }
        });
    }

    @Override
    public void onMessage(final MessageEvent<PircBotX> event) {
        sendMessage(event.getChannel().getName(), event);
    }

    @Override
    public void onAction(final ActionEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            onPrivateEvent(event.getUser(), event.getAction(), event);
        } else {
            sendMessage(event.getChannel().getName(), event);
        }
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        sendMessage(event.getChannel().getName(), event);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    arrayAdapter.replace(oldFormattedNick, newFormattedNick);
                }
            }
        });
    }

    @Override
    public void onOtherUserJoin(final JoinEvent<PircBotX> event) {
        sendMessage(event.getChannel().getName(), event);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    arrayAdapter.add(event.getUser().getPrettyNick(event.getChannel()));
                    arrayAdapter.sort();
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onOtherUserPart(final PartEvent<PircBotX> event) {
        sendMessage(event.getChannel().getName(), event);

        recreateUserList(event.getChannel(), event.getUser());
    }

    @Override
    protected void onUserPart(final PartEvent<PircBotX> event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = mIRCPagerAdapter.removeView(event.getChannel().getName());
                getActivity().removeTab(index);
            }
        });
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event) {
        sendMessage(event.getChannel().getName(), event);

        recreateUserList(event.getChannel(), event.getUser());
    }

    public void recreateUserList(final Channel channel, final User user) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(channel.getName())) {
                    arrayAdapter.remove(user.getPrettyNick(channel));
                    arrayAdapter.sort();
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Private message events
    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        onPrivateEvent(event.getUser(), event.getMessage(), event);
    }

    private void onPrivateEvent(final User user, final String message, final Event<PircBotX> event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final IRCFragment fragment = privateMessageCheck(user.getNick());
                if (fragment != null) {
                    final PMFragment pm = (PMFragment) fragment;
                    if (!message.equals("")) {
                        pm.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
                    }
                } else {
                    getActivity().onNewPrivateMessage(user.getNick());
                }
            }
        });
    }

    // Misc stuff
    private void sendMessage(final String title, final Event event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final IRCFragment channel = mIRCPagerAdapter.getTab(title);
                if (channel != null) {
                    channel.appendToTextView(EventParser.getOutputForEvent(event, getActivity()));
                }
            }
        });
    }

    private boolean checkChannelFragment(final String keyName) {
        final int position = mViewPager.getCurrentItem();
        final IRCFragment frag = (IRCFragment) mIRCPagerAdapter.getItem(position);
        return frag.getTitle().equals(keyName);
    }

    private IRCFragment privateMessageCheck(final String userNick) {
        return mIRCPagerAdapter.getTab(userNick);
    }
}