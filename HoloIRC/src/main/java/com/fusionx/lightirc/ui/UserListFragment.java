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
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ChannelUser;
import com.fusionx.relay.event.channel.ChannelNameEvent;
import com.fusionx.relay.event.channel.ChannelWorldUserEvent;
import com.fusionx.relay.misc.IRCUserComparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class UserListFragment extends Fragment implements AbsListView.MultiChoiceModeListener,
        AdapterView.OnItemClickListener {

    private ActionMode mActionMode;

    private Callback mCallback;

    private Channel mChannel;

    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            // If it's null then remove the old conversation
            if (conversationChanged.conversation == null
                    || conversationChanged.fragmentType != FragmentType.CHANNEL) {
                if (mChannel != null) {
                    mChannel.getServer().getServerEventBus().unregister(UserListFragment.this);
                }
                mChannel = null;
                return;
            }
            if (mChannel != null) {
                mChannel.getServer().getServerEventBus().unregister(UserListFragment.this);
            }

            mChannel = (Channel) conversationChanged.conversation;
            mChannel.getServer().getServerEventBus().register(UserListFragment.this);
            onUpdateUserList();
        }
    };

    private UserListAdapter mAdapter;

    private TreeSet<ChannelUser> mChannelUsers;

    private StickyListHeadersListView mStickyListView;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = FragmentUtils.getParent(this, Callback.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.default_stickylist_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStickyListView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
        mAdapter = new UserListAdapter(view.getContext(), mChannelUsers);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().getWrappedList().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(this);
        getListView().setFastScrollEnabled(true);
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
            mChannel.getServer().getServerEventBus().unregister(this);
        }
        // Don't keep a track of this channel - we will deal with this when we return
        mChannel = null;
    }

    public void onUpdateUserList() {
        mCallback.updateUserListVisibility();

        final Collection<? extends ChannelUser> userList = mChannel.getUsers();

        getListView().setAdapter(null);
        mAdapter.setChannel(mChannel);
        mChannelUsers = new TreeSet<>(new IRCUserComparator(mChannel));
        mChannelUsers.addAll(userList);
        mAdapter.setInternalSet(mChannelUsers);
        getListView().setAdapter(mAdapter);

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
    @SuppressWarnings("unused")
    public void onEventMainThread(final ChannelWorldUserEvent event) {
        if (event.channel.equals(mChannel) && event.isUserListChangeEvent()) {
            onUpdateUserList();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final ChannelNameEvent event) {
        if (event.channel.equals(mChannel)) {
            onUpdateUserList();
        }
    }
    // End of subscribed events

    public StickyListHeadersListView getListView() {
        return mStickyListView;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
            boolean checked) {
        int selectedItemCount = getCheckedItems().size();

        if (selectedItemCount != 0) {
            final String quantityString = getResources()
                    .getQuantityString(R.plurals.user_selection,
                            selectedItemCount, selectedItemCount);
            mode.setTitle(quantityString);

            mode.getMenu().getItem(1).setVisible(selectedItemCount == 1);
            mode.getMenu().getItem(2).setVisible(selectedItemCount == 1);
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mActionMode = mode;

        final MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.fragment_userlist_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final List<ChannelUser> selectedItems = getCheckedItems();
        final String nick = selectedItems.get(0).getNick().getNickAsString();
        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mCallback.onMentionMultipleUsers(selectedItems);
                mode.finish();
                mCallback.closeDrawer();
                return true;
            case R.id.fragment_userlist_cab_pm: {
                onPrivateMessageUser(nick);
                return true;
            }
            case R.id.fragment_userlist_cab_whois:
                mChannel.getServer().getServerCallHandler().sendUserWhois(nick);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final boolean checked = getListView().getCheckedItemPositions().get(position);
        getListView().setItemChecked(position, !checked);

        if (mActionMode == null) {
            getActivity().startActionMode(this);
        }
    }

    protected List<ChannelUser> getCheckedItems() {
        final List<ChannelUser> checkedSessionPositions = new ArrayList<>();
        if (mStickyListView == null) {
            return checkedSessionPositions;
        }

        final SparseBooleanArray checkedPositionsBool = mStickyListView.getCheckedItemPositions();
        for (int i = 0; i < checkedPositionsBool.size(); i++) {
            if (checkedPositionsBool.valueAt(i)) {
                checkedSessionPositions.add(mAdapter.getItem(checkedPositionsBool.keyAt(i)));
            }
        }

        return checkedSessionPositions;
    }

    boolean isNickOtherUsers(final String nick) {
        return !mChannel.getServer().getUser().getNick().getNickAsString().equals(nick);
    }

    private void onPrivateMessageUser(final String nick) {
        if (isNickOtherUsers(nick)) {
            mChannel.getServer().getServerCallHandler().sendMessageToQueryUser(nick, "");
            mCallback.closeDrawer();
            mActionMode.finish();
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