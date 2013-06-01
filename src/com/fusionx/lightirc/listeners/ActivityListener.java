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
import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.misc.UserComparator;
import com.fusionx.lightirc.misc.Utils;
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
    private UserListAdapter adapter;

    public ActivityListener(ServerChannelActivity a, IRCPagerAdapter d, ViewPager pager) {
        activity = a;
        mIRCPagerAdapter = d;
        mViewPager = pager;
    }

    public void setArrayAdapter(final UserListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onEvent(final Event<LightBot> event) throws Exception {
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

    @Override
    public void onMessage(final MessageEvent<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);
    }

    @Override
    public void onAction(final ActionEvent<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);
    }

    @Override
    public void userJoin(final JoinEvent<LightBot> event) {
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
    public void otherUserJoin(final JoinEvent<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    adapter.add(event.getUser().getPrettyNick(event.getChannel()));
                    adapter.sort();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void part(final PartEvent<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    adapter.remove(event.getUser().getPrettyNick(event.getChannel()));
                    adapter.sort();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onUserList(final UserListEvent<LightBot> event) {
        final ArrayList<String> userList = event.getChannel().getUserList();
        final String channelName = event.getChannel().getName();

        for (final User u : event.getUsers()) {
            userList.add(u.getPrettyNick(event.getChannel()));
        }

        Collections.sort(userList, new UserComparator());

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ChannelFragment) mIRCPagerAdapter.getTab(channelName)).setUserList(userList);
            }
        });
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        final String channelName = ((TopicEvent) event).getChannel().getName();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final IRCFragment channel = mIRCPagerAdapter.getTab(channelName);
                        channel.writeToTextView(Utils.getOutputForEvent(event));
                    }
                }, 750);
            }
        });
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<LightBot> event) {
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        sendMessage(event.getChannel().getName(), event);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    adapter.replace(oldFormattedNick, newFormattedNick);
                }
            }
        });
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<LightBot> event) {
        sendMessage(event.getChannel().getName(), event);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkChannelFragment(event.getChannel().getName())) {
                    adapter.remove(event.getUser().getPrettyNick(event.getChannel()));
                    adapter.sort();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    // TODO - standardise this and private actions
    @Override
    public void onPrivateMessage(final PrivateMessageEvent<LightBot> event) {
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

    @Override
    public void onPrivateAction(final PrivateActionEvent<LightBot> event) {
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

    private void sendMessage(final String title, final Event event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final IRCFragment channel = mIRCPagerAdapter.getTab(title);
                channel.writeToTextView(Utils.getOutputForEvent(event));
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