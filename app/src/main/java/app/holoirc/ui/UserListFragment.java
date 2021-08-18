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

package app.holoirc.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import app.holoirc.R;
import app.holoirc.event.OnConversationChanged;
import app.holoirc.misc.FragmentType;
import app.holoirc.util.FragmentUtils;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.base.Channel;
import co.fusionx.relay.base.Nick;
import co.fusionx.relay.constants.UserLevel;
import co.fusionx.relay.event.channel.ChannelNameEvent;
import co.fusionx.relay.event.channel.ChannelNickChangeEvent;
import co.fusionx.relay.event.channel.ChannelUserLevelChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldJoinEvent;
import co.fusionx.relay.event.channel.ChannelWorldKickEvent;
import co.fusionx.relay.event.channel.ChannelWorldLevelChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldNickChangeEvent;
import co.fusionx.relay.event.channel.ChannelWorldPartEvent;
import co.fusionx.relay.event.channel.ChannelWorldQuitEvent;

import static app.holoirc.util.MiscUtils.getBus;

public class UserListFragment extends Fragment {

    private Callback mCallback;

    private Channel mChannel;

    private UserListAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private final List<Pair<Nick, UserLevel>> mCheckedPositions = new ArrayList<>();

    private final ActionModeHandler mActionModeHandler = new ActionModeHandler(mCheckedPositions);

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
            mCallback.updateUserListVisibility();
        }
    };

    private void updateAdapter(final Channel channel) {
        mAdapter = channel == null ? null : new UserListAdapter(
                getActivity(),
                channel,
                mCheckedPositions,
                mActionModeHandler,
                mActionModeHandler);
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

    /*
     * Subscribed events
     *
     * Only perform an action if the channel that is being observed currently is the one the
     * event is referring to
     */
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldJoinEvent event) {
        mActionModeHandler.finish();
        mAdapter.addUser(event.user, event.user.getChannelPrivileges(event.channel));
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldKickEvent event) {
        mActionModeHandler.finish();
        mAdapter.removeUser(event.user, event.level);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldLevelChangeEvent event) {
        mActionModeHandler.finish();
        mAdapter.changeMode(event.user, event.oldLevel, event.newLevel);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldNickChangeEvent event) {
        mActionModeHandler.finish();
        mAdapter.changeNick(event.user, event.oldNick, event.userNick);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldPartEvent event) {
        mActionModeHandler.finish();
        mAdapter.removeUser(event.user, event.level);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelWorldQuitEvent event) {
        mActionModeHandler.finish();
        mAdapter.removeUser(event.user, event.level);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelNickChangeEvent event) {
        mActionModeHandler.finish();
        mAdapter.changeNick(event.relayUser, event.oldNick, event.newNick);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelUserLevelChangeEvent event) {
        mActionModeHandler.finish();
        mAdapter.changeMode(event.user, event.oldLevel, event.newLevel);
        mCallback.updateUserListVisibility();
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final ChannelNameEvent event) {
        mActionModeHandler.finish();
        updateAdapter(mChannel);
        mCallback.updateUserListVisibility();
    }
    // End of subscribed events

    boolean isNickOtherUsers(final Nick nick) {
        return !mChannel.getServer().getUser().getNick().equals(nick);
    }

    private void onPrivateMessageUser(final Nick nick) {
        if (isNickOtherUsers(nick)) {
            mChannel.getServer().sendQuery(nick.getNickAsString(), null);
            mCallback.closeDrawer();
            mActionModeHandler.finish();
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

    public void finishCab() {
        mActionModeHandler.finish();
    }

    public interface Callback {

        void onMentionMultipleUsers(final List<Nick> users);

        void updateUserListVisibility();

        void closeDrawer();
    }

    private class ActionModeHandler implements View.OnClickListener, View.OnLongClickListener,
            ActionMode.Callback {

        private final List<Pair<Nick, UserLevel>> mCheckedPositions;

        private ActionMode mActionMode;

        public ActionModeHandler(List<Pair<Nick, UserLevel>> checkedPositions) {
            mCheckedPositions = checkedPositions;
        }

        @Override
        public void onClick(View v) {
            Pair<Nick, UserLevel> user = (Pair<Nick, UserLevel>) v.getTag();
            onViewSelected(v, user);
        }

        @Override
        public boolean onLongClick(View v) {
            Pair<Nick, UserLevel> user = (Pair<Nick, UserLevel>) v.getTag();
            onViewSelected(v, user);
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.fragment_userlist_cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedItemCount = mCheckedPositions.size();
            if (selectedItemCount != 0) {
                final String quantityString = getResources()
                        .getQuantityString(R.plurals.user_selection,
                                selectedItemCount, selectedItemCount);
                mode.setTitle(quantityString);

                mode.getMenu().getItem(1).setVisible(selectedItemCount == 1);
                mode.getMenu().getItem(2).setVisible(selectedItemCount == 1);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.fragment_userlist_cab_mention:
                    mCallback.onMentionMultipleUsers(getAllNicks());
                    break;
                case R.id.fragment_userlist_cab_pm:
                    onPrivateMessageUser(getFirstCheckedNick());
                    break;
                case R.id.fragment_userlist_cab_whois:
                    mChannel.getServer().sendWhois(getFirstCheckedNick().getNickAsString());
                    break;
                default:
                    return false;
            }

            mode.finish();
            mCallback.closeDrawer();
            return true;
        }

        private Nick getFirstCheckedNick() {
            return mCheckedPositions.get(0).first;
        }

        private List<Nick> getAllNicks() {
            final List<Nick> nicks = new ArrayList<>();
            for (Pair<Nick, UserLevel> i : mCheckedPositions) {
                nicks.add(i.first);
            }
            return nicks;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mCheckedPositions.clear();
            mAdapter.notifyDataSetChanged();
            mActionMode = null;
        }

        public void finish() {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }

        private void onViewSelected(View view, Pair<Nick, UserLevel> position) {
            boolean remove = mCheckedPositions.contains(position);
            if (remove) {
                mCheckedPositions.remove(position);
                if (mCheckedPositions.isEmpty()) {
                    finish();
                } else {
                    mActionMode.invalidate();
                }
            } else {
                mCheckedPositions.add(position);
                if (mActionMode == null) {
                    mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
                }
                mActionMode.invalidate();
            }
            view.setActivated(!remove);
        }
    }
}
