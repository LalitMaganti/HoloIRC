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

import android.app.Activity;
import android.content.Context;
import com.fusionx.lightirc.fragments.ircfragments.ChannelFragment;
import com.fusionx.lightirc.fragments.ircfragments.IRCFragment;
import com.fusionx.lightirc.fragments.ircfragments.PMFragment;
import com.fusionx.lightirc.fragments.ircfragments.ServerFragment;
import com.fusionx.lightirc.interfaces.CommonIRCListenerInterface;
import com.fusionx.lightirc.parser.EventParser;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.IOExceptionEvent;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.NickInUseEvent;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

public class ActivityListener extends GenericListener {
    private final Context mContext;
    private final ActivityListenerInterface mListener;
    private final CommonIRCListenerInterface mCommonListener;

    public ActivityListener(final Activity activity) {
        super(activity.getApplicationContext());

        mContext = activity.getApplicationContext();
        try {
            mListener = (ActivityListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ActivityListenerInterface");
        }
        try {
            mCommonListener = (CommonIRCListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CommonIRCListenerInterface");
        }
    }

    // Server stuff
    @Override
    public void onConnect(final ConnectEvent<PircBotX> event) {
        onServerMessage(event);

        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCommonListener.closeAllSlidingMenus();
                mListener.onConnect();
            }
        });
    }

    // This HAS to be an unexpected disconnect. If it isn't then there's something wrong.
    @Override
    public void onDisconnect(final DisconnectEvent<PircBotX> event) {
        onServerMessage(event);

        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onUnexpectedDisconnect();
            }
        });
    }

    @Override
    protected void onIOException(final IOExceptionEvent<PircBotX> event) {
        onServerMessage(event);

        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onUnexpectedDisconnect();
            }
        });
    }

    // Server events
    @Override
    public void onNotice(final NoticeEvent<PircBotX> event) {
        if (event.getChannel() == null) {
            if (event.getUser().getBuffer().isEmpty()) {
                onServerMessage(event);
            } else {
                onPrivateEvent(event, event.getUser(), event.getNotice());
            }
        } else {
            onChannelMessage(event, event.getChannel());
        }
    }

    @Override
    public void onUnknown(final UnknownEvent<PircBotX> event) {
        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.selectServerFragment();
            }
        });

        super.onUnknown(event);
    }

    // Channel events
    @Override
    public void onBotJoin(final JoinEvent<PircBotX> event) {
        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onNewChannelJoined(event.getChannel().getName(), true);
            }
        });
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event) {
        onUserListChanged(event.getChannel());
    }

    @Override
    public void onOtherUserJoin(final JoinEvent<PircBotX> event) {
        onUserListChanged(event.getChannel());
    }

    @Override
    public void onOtherUserPart(final PartEvent<PircBotX> event) {
        onUserListChanged(event.getChannel());
    }

    @Override
    public void onNickInUse(NickInUseEvent<PircBotX> event) {
        mListener.selectServerFragment();

        super.onNickInUse(event);
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event) {
        onUserListChanged(event.getChannel());
    }

    @Override
    public void onMode(final ModeEvent<PircBotX> event) {
        if (event.getUser() != null) {
            super.onMode(event);

            onUserListChanged(event.getChannel());
        }
    }

    @Override
    public void onUserList(final UserListEvent<PircBotX> event) {
        onUserListChanged(event.getChannel());
    }

    private void onUserListChanged(final Channel channel) {
        if (mListener.isFragmentSelected(channel.getName())) {
            mListener.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCommonListener.closeAllSlidingMenus();
                }
            });
        }
    }

    @Override
    public void onUserPart(final PartEvent<PircBotX> event) {
        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.switchFragmentAndRemove(event.getChannel().getName());
            }
        });
    }

    @Override
    void onPrivateEvent(final Event<PircBotX> event, final User user, final String message) {
        final IRCFragment fragment = mListener.isFragmentAvailable(user.getNick());
        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fragment != null && fragment instanceof PMFragment) {
                    if (!message.equals("")) {
                        onUserMessage(event, user);
                    }
                } else {
                    mCommonListener.onCreatePMFragment(user.getNick());
                }
            }
        });
    }

    public void onServerMessage(final Event<PircBotX> event) {
        final IRCFragment fragment = mListener.isFragmentAvailable(event.getBot().getConfiguration().getTitle());
        if (fragment != null && fragment instanceof ServerFragment) {
            mListener.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.appendToTextView(EventParser.getOutputForEvent(event, mContext));
                }
            });
        }
    }

    @Override
    void onChannelMessage(final Event<PircBotX> event, Channel channel) {
        final IRCFragment fragment = mListener.isFragmentAvailable(channel.getName());
        if (fragment != null && fragment instanceof ChannelFragment) {
            mListener.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.appendToTextView(EventParser.getOutputForEvent(event, mContext));
                }
            });
        }
    }

    @Override
    void onUserMessage(Event<PircBotX> event, User user) {
        final IRCFragment fragment = mListener.isFragmentAvailable(user.getNick());
        fragment.appendToTextView(EventParser.getOutputForEvent(event, mContext));
    }

    public interface ActivityListenerInterface {
        public void onConnect();

        public void onNewChannelJoined(final String channelName, final boolean forceSwitch);

        public void onUnexpectedDisconnect();

        public void runOnUiThread(final Runnable runnable);

        public boolean isFragmentSelected(final String title);

        public IRCFragment isFragmentAvailable(final String title);

        public void selectServerFragment();

        public void switchFragmentAndRemove(final String channelName);
    }
}