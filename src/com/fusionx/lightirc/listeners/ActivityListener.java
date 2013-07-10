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
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.EventParser;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.IOExceptionEvent;
import org.pircbotx.hooks.events.lightirc.IrcExceptionEvent;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
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
        appendToServer(event);

        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCommonListener.closeAllSlidingMenus();
            }
        });
    }

    // This HAS to be an unexpected disconnect. If it isn't then there's something wrong.
    @Override
    public void onDisconnect(final DisconnectEvent<PircBotX> event) {
        appendToServer(event);

        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onUnexpectedDisconnect();
            }
        });
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
        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.selectServerFragment();
            }
        });
        appendToServer(event);
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
        if (mListener.isFragmentSelected(channelName)) {
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
                mListener.removeFragment(event.getChannel().getName());
            }
        });
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        onPrivateEvent(event.getUser(), event.getMessage(), event);
    }

    // Misc stuff
    private void onPrivateEvent(final User user, final String message, final Event<PircBotX> event) {
        final IRCFragment fragment = mListener.isFragmentAvailable(user.getNick());
        mListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fragment != null && fragment instanceof PMFragment) {
                    if (!message.equals("")) {
                        fragment.appendToTextView(EventParser.getOutputForEvent(event, mContext));
                    }
                } else {
                    mCommonListener.onCreatePMFragment(user.getNick());
                }
            }
        });
    }

    private void appendToServer(final Event<PircBotX> event) {
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

    private void onChannelMessage(final String channelName, final Event<PircBotX> event) {
        final IRCFragment fragment = mListener.isFragmentAvailable(channelName);
        if (fragment != null && fragment instanceof ChannelFragment) {
            mListener.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.appendToTextView(EventParser.getOutputForEvent(event, mContext));
                }
            });
        }
    }

    public interface ActivityListenerInterface {
        public void onCreateChannelFragment(final String channelName);

        public void onNewChannelJoined(final String channelName, final boolean forceSwitch);

        public void onUnexpectedDisconnect();

        public void runOnUiThread(final Runnable runnable);

        public boolean isFragmentSelected(final String title);

        public IRCFragment isFragmentAvailable(final String title);

        public void selectServerFragment();

        public void removeFragment(final String channelName);
    }
}