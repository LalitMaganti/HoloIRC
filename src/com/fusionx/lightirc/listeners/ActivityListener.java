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
import android.widget.ArrayAdapter;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.fragments.ChannelFragment;
import com.fusionx.lightirc.fragments.IRCFragment;
import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.misc.Utils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.ArrayList;

public class ActivityListener extends ListenerAdapter<LightBot> implements Listener<LightBot> {
    private final ServerChannelActivity activity;
    private final IRCPagerAdapter mIRCPagerAdapter;
    private final ViewPager mViewPager;
    private ArrayAdapter<String> adapter;

    public ActivityListener(ServerChannelActivity a, IRCPagerAdapter d, ViewPager pager) {
        activity = a;
        mIRCPagerAdapter = d;
        mViewPager = pager;
    }

    public void setArrayAdapter(ArrayAdapter<String> adapter) {
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
    public void onQuit(final QuitEvent<LightBot> event) {
        // Keep this code up to date with ChannelListener
        for (final Channel c : event.getUser().getChannels()) {
            if (event.getDaoSnapshot().getChannel(c.getName()).getUsers().contains(event.getBot().getUserBot())) {
                sendMessage(c.getName(), event);
            }
        }
    }

    @Override
    public void onPart(final PartEvent<LightBot> event) {
        if (!event.getUser().getNick().equals(event.getBot().getNick())) {
            sendMessage(event.getChannel().getName(), event);
        }
    }

    @Override
    public void onNickChange(final NickChangeEvent<LightBot> event) {
        // Keep this code up to date with ChannelListener
        for (final Channel c : event.getBot().getUserBot().getChannels()) {
            final ArrayList<String> set = ((LightChannel) c).getCleanUserNicks();
            if (set.contains(event.getOldNick()) || set.contains(event.getNewNick())) {
                sendMessage(c.getName(), event);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int position = mViewPager.getCurrentItem();
                        IRCFragment frag = (IRCFragment) mIRCPagerAdapter.getItem(position);
                        if (frag.getTitle().equals(c.getName())) {
                            adapter.remove(event.getOldNick());
                            adapter.add(event.getNewNick());
                            adapter.notifyDataSetChanged();
                            ((ChannelFragment) frag).getUserList().remove(event.getOldNick());
                            ((ChannelFragment) frag).getUserList().add(event.getNewNick());
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onJoin(final JoinEvent<LightBot> event) {
        if (!((JoinEvent) event).getUser().getNick().equals(event.getBot().getNick())) {
            sendMessage(event.getChannel().getName(), event);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.onNewChannelJoined(((JoinEvent) event).getChannel().getName(), ((JoinEvent) event)
                            .getUser().getNick(), Utils.getOutputForEvent(event));
                }
            });
        }
    }

    @Override
    public void onUserList(final UserListEvent<LightBot> event) {
        final ArrayList<String> users = new ArrayList<String>();
        for(User u : event.getUsers()) {
            users.add(u.getNick());
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ChannelFragment) mIRCPagerAdapter.getTab(event.getChannel().getName())).setUserList(users);
            }
        });
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final IRCFragment channel = mIRCPagerAdapter.getTab(((TopicEvent) event).getChannel().getName());
                        channel.writeToTextView(Utils.getOutputForEvent(event));
                    }
                }, 1500);
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
}