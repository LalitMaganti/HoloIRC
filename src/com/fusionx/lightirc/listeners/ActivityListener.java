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

import android.os.Handler;
import android.support.v4.view.ViewPager;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.fragments.ChannelFragment;
import com.fusionx.lightirc.fragments.IRCFragment;
import com.fusionx.lightirc.fragments.PMFragment;
import com.fusionx.lightirc.misc.UserComparator;
import com.fusionx.lightirc.misc.Utils;
import lombok.AccessLevel;
import lombok.Setter;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.PartEvent;
import org.pircbotx.hooks.events.lightirc.PrivateActionEvent;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

import java.util.ArrayList;
import java.util.Collections;

public class ActivityListener extends GenericListener {
    private final ServerChannelActivity activity;
    private final IRCPagerAdapter mIRCPagerAdapter;
    private final ViewPager mViewPager;

    @Setter(AccessLevel.PUBLIC)
    private UserListAdapter arrayAdapter;

    public ActivityListener(ServerChannelActivity a, IRCPagerAdapter d, ViewPager pager) {
        activity = a;
        mIRCPagerAdapter = d;
        mViewPager = pager;
    }

    // Server events
    @Override
    public void onEvent(final Event<PircBotX> event) throws Exception {
        super.onEvent(event);

        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            final IRCFragment server = (IRCFragment) mIRCPagerAdapter.getItem(0);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server.writeToTextView(Utils.getOutputForEvent(event));
                }
            });
        }
    }

    // Channel events
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        final JoinEvent joinevent = (JoinEvent) event;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.onNewChannelJoined(joinevent.getChannel().getName(),
                        Utils.getOutputForEvent(event), null);
            }
        });
    }

    @Override
    public void onTopic(final TopicEvent<PircBotX> event) {
        final String channelName = ((TopicEvent) event).getChannel().getName();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final IRCFragment channel = mIRCPagerAdapter.getTab(channelName);
                        if(channel != null) {
                            channel.writeToTextView(Utils.getOutputForEvent(event));
                        }
                    }
                }, 750);
            }
        });
    }

    @Override
    public void onUserList(final UserListEvent<PircBotX> event) {
        final ArrayList<String> userList = event.getChannel().getUserList();
        final String channelName = event.getChannel().getName();

        for (final User u : event.getUsers()) {
            userList.add(u.getPrettyNick(event.getChannel()));
        }

        Collections.sort(userList, new UserComparator());

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ChannelFragment channel = (ChannelFragment) mIRCPagerAdapter
                        .getTab(channelName);
                if(channel != null) {
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
        sendMessage(event.getChannel().getName(), event);
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        sendMessage(event.getChannel().getName(), event);

        activity.runOnUiThread(new Runnable() {
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

        activity.runOnUiThread(new Runnable() {
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

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    arrayAdapter.remove(event.getUser().getPrettyNick(event.getChannel()));
                    arrayAdapter.sort();
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event) {
        sendMessage(event.getChannel().getName(), event);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    arrayAdapter.remove(event.getUser().getPrettyNick(event.getChannel()));
                    arrayAdapter.sort();
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Private message events
    // TODO - standardise this and private actions
    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IRCFragment fragment = privateMessageCheck(event.getUser().getNick());
                if (fragment != null) {
                    PMFragment pm = (PMFragment) fragment;
                    if(!event.getMessage().equals("")) {
                        pm.writeToTextView(Utils.getOutputForEvent(event));
                    }
                } else {
                    activity.onNewPrivateMessage(event.getUser().getNick(),
                            Utils.getOutputForEvent(event));
                }
            }
        });
    }

    @Override
    public void onPrivateAction(final PrivateActionEvent<PircBotX> event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IRCFragment fragment = privateMessageCheck(event.getUser().getNick());
                if (fragment != null) {
                    PMFragment pm = (PMFragment) fragment;
                    pm.writeToTextView(Utils.getOutputForEvent(event));
                } else {
                    activity.onNewPrivateMessage(event.getUser().getNick(),
                            Utils.getOutputForEvent(event));
                }
            }
        });
    }

    // Misc stuff
    private void sendMessage(final String title, final Event event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final IRCFragment channel = mIRCPagerAdapter.getTab(title);
                if(channel != null) {
                    channel.writeToTextView(Utils.getOutputForEvent(event));
                }
            }
        });
    }

    private boolean checkChannelFragment(String keyName) {
        int position = mViewPager.getCurrentItem();
        IRCFragment frag = (IRCFragment) mIRCPagerAdapter.getItem(position);
        return frag.getTitle().equals(keyName);
    }

    private IRCFragment privateMessageCheck(String userNick) {
        return mIRCPagerAdapter.getTab(userNick);
    }
}