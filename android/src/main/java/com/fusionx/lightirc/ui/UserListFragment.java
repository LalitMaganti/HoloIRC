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

package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.FragmentUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
import co.fusionx.relay.base.IRCConnection;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.event.channel.ChannelNameEvent;
import co.fusionx.relay.event.channel.ChannelNickChangeEvent;
import co.fusionx.relay.event.channel.ChannelUserLevelChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldJoinEvent;
import co.fusionx.relay.event.channel.ChannelWorldKickEvent;
import co.fusionx.relay.event.channel.ChannelWorldLevelChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldNickChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldPartEvent;
import co.fusionx.relay.event.channel.ChannelWorldQuitEvent;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class UserListFragment extends Fragment {

    private Callback mCallback;

    private Channel mChannel;

    private IRCConnection mConnection;

    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            if (mChannel != null) {
                mChannel.getBus().unregister(UserListFragment.this);
            }

            if (conversationChanged.fragmentType == FragmentType.CHANNEL) {
                mConnection = conversationChanged.connection;
                mChannel = (Channel) conversationChanged.conversation;
                mChannel.getBus().register(UserListFragment.this);
            } else {
                mConnection = null;
                mChannel = null;
            }

            updateAdapter(mChannel);
            onUserListChanged();
        }
    };

    private UserListAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private void updateAdapter(final Channel channel) {
        mAdapter = channel == null ? null : new UserListAdapter(getActivity(), channel);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = FragmentUtils.getParent(this, Callback.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        updateAdapter(mChannel);
    }

    @Override
    public void onResume() {
        super.onResume();

        // On resume, we may have missed events in the background - make sure we have the most
        // up-to-date user list - register the event handler sticky so we can get the latest info
        getBus().registerSticky(mEventHandler);
    }

    @Override
    public void onPause() {
        super.onPause();

        getBus().unregister(mEventHandler);

        // On a pause, it could lead to a stop in which case we don't actually know what's going
        // on in the background - stop observation and restart when we return
        if (mChannel != null) {
            mChannel.getBus().unregister(this);
        }
        // Don't keep a track of this connection/channel - we will deal with this when we return
        mConnection = null;
        mChannel = null;
    }

    public void onUserListChanged() {
        mCallback.updateUserListVisibility();
    }

    /*
     * Subscribed events
     *
     * Only perform an action if the channel that is being observed currently is the one the
     * event is referring to
     */
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldJoinEvent event) {
        mAdapter.addUser(event.user, event.user.getChannelPrivileges(event.channel));
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldKickEvent event) {
        mAdapter.removeUser(event.user, event.level);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldLevelChangeEvent event) {
        mAdapter.changeMode(event.user, event.oldLevel, event.newLevel);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldNickChangeEvent event) {
        mAdapter.changeNick(event.user, event.oldNick, event.userNick);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldPartEvent event) {
        mAdapter.removeUser(event.user, event.level);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldQuitEvent event) {
        mAdapter.removeUser(event.user, event.level);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelNickChangeEvent event) {
        mAdapter.changeNick(event.relayUser, event.oldNick, event.newNick);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelUserLevelChangeEvent event) {
        mAdapter.changeMode(event.user, event.oldLevel, event.newLevel);
        onUserListChanged();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelNameEvent event) {
        updateAdapter(mChannel);
        onUserListChanged();
    }
    // End of subscribed events

    boolean isNickOtherUsers(final Nick nick) {
        return !mConnection.getUserChannelDao().getUser().getNick().equals(nick);
    }

    private void onPrivateMessageUser(final Nick nick) {
        if (isNickOtherUsers(nick)) {
            mConnection.getServer().sendQuery(nick.getNickAsString(), null);
            mCallback.closeDrawer();
        } else {
            final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setTitle(getActivity().getString(R.string.user_list_not_possible))
                    .setMessage(getActivity()
                            .getString(R.string.user_list_pm_self_not_possible))
                    .setPositiveButton(getActivity().getString(R.string.ok),
                            (dialogInterface, i) -> dialogInterface.dismiss()
                    );
            build.show();
        }
    }

    public interface Callback {

        public void onMentionMultipleUsers(final List<ChannelUser> users);

        public void updateUserListVisibility();

        public void closeDrawer();
    }
}