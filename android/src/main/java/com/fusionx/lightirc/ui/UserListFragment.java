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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.FragmentUtils;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.ChannelUser;
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

public class UserListFragment extends Fragment implements ActionMode.Callback {

    private Callback mCallback;

    private Channel mChannel;

    private ActionMode mActionMode;

    private UserListAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            if (mChannel != null) {
                mChannel.getBus().unregister(UserListFragment.this);
            }

            if (conversationChanged.fragmentType == FragmentType.CHANNEL) {
                mChannel = (Channel) conversationChanged.conversation;
                mChannel.getBus().register(UserListFragment.this);
            } else {
                mChannel = null;
            }

            updateAdapter(mChannel);
            onUserListChanged();
        }
    };

    private View mCheckedView;

    private void updateAdapter(final Channel channel) {
        mAdapter = channel == null ? null : new UserListAdapter(getActivity(), channel,
                v -> {
                    if (mActionMode == null) {
                        return;
                    } else if (mCheckedView == null) {
                        mActionMode.finish();
                        return;
                    }

                    mCheckedView.setActivated(false);
                    mCheckedView = v;
                    mCheckedView.setActivated(true);
                },
                v -> {
                    mCheckedView = v;
                    mCheckedView.setActivated(true);
                    if (mActionMode == null) {
                        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
                    }
                    return true;
                });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        mCallback = FragmentUtils.getParent(this, Callback.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user_list_fragment, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        // Don't keep a track of this channel - we will deal with this when we return
        mChannel = null;
    }

    public void onUserListChanged() {
        mCallback.updateUserListVisibility();
        if (mActionMode != null) {
            mActionMode.finish();
        }
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
        return !mChannel.getServer().getUser().getNick().equals(nick);
    }

    private void onPrivateMessageUser(final Nick nick) {
        if (isNickOtherUsers(nick)) {
            mChannel.getServer().sendQuery(nick.getNickAsString(), null);
            mCallback.closeDrawer();
            if (mActionMode != null) {
                mActionMode.finish();
            }
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.user_list_not_possible))
                    .setMessage(getActivity()
                            .getString(R.string.user_list_pm_self_not_possible))
                    .setPositiveButton(getActivity().getString(R.string.ok),
                            (dialogInterface, i) -> dialogInterface.dismiss()
                    )
                    .show();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.fragment_userlist_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final Optional<ChannelUser> user = Optional.fromNullable(mCheckedView)
                .transform(new Function<View, Nick>() {
                    @Nullable
                    @Override
                    public Nick apply(View input) {
                        return (Nick) input.getTag();
                    }
                }).transform(new Function<Nick, ChannelUser>() {
                    @Nullable
                    @Override
                    public ChannelUser apply(Nick input) {
                        final Optional<? extends ChannelUser> user =
                                mChannel.getServer().getUserChannelInterface().getUser(
                                        input.getNickAsString());
                        return user.isPresent() ? user.get() : null;
                    }
                });
        if (!user.isPresent()) {
            mode.finish();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mCallback.onMentionMultipleUsers(Collections.singletonList(user.get()));
                break;
            case R.id.fragment_userlist_cab_pm:
                onPrivateMessageUser(user.get().getNick());
                break;
            case R.id.fragment_userlist_cab_whois:
                mChannel.getServer().sendWhois(user.get().getNick().getNickAsString());
                break;
            default:
                return false;
        }
        mode.finish();
        mCallback.closeDrawer();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (mCheckedView != null) {
            mCheckedView.setActivated(false);
            mCheckedView = null;
        }
        mActionMode = null;
    }

    public void finishCab() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public interface Callback {

        public void onMentionMultipleUsers(final List<ChannelUser> users);

        public void updateUserListVisibility();

        public void closeDrawer();
    }
}
