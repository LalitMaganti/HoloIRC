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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.channel.WorldUserEvent;
import com.fusionx.relay.misc.IRCUserComparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class UserListFragment extends Fragment implements AbsListView.MultiChoiceModeListener {

    private ActionMode mActionMode;

    private Callback mCallback;

    private Channel mChannel;

    private Object mEventHandler = new Object() {
        @SuppressWarnings("unused")
        public void onEvent(final OnConversationChanged conversationChanged) {
            if (conversationChanged.fragmentType == FragmentType.CHANNEL) {
                mChannel = (Channel) conversationChanged.conversation;
            }
        }
    };

    private UserListAdapter mAdapter;

    private TreeSet<WorldUser> mWorldUsers;

    private StickyListHeadersListView mStickyListView;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mCallback = FragmentUtils.getParent(this, Callback.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_userlist_listview, container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventBus.getDefault().registerSticky(mEventHandler);
        mWorldUsers = new TreeSet<>(new IRCUserComparator(mChannel));

        mStickyListView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
        mAdapter = new UserListAdapter(view.getContext(), mWorldUsers);
        mAdapter.setInternalSet(mWorldUsers);

        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        // On a pause, it could lead to a stop in which case we don't actually know what's going
        // on in the background - stop observation and restart when we return
        if (mChannel != null) {
            onStopObserving();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // On resume, we may have missed events in the background - make sure we have the most
        // up-to-date user list
        if (mChannel != null) {
            onStartObserving();
            onUpdateUserList();
        }
    }

    public void onUpdateUserList() {
        final Collection<WorldUser> userList = mChannel.getUsers();

        getListView().setAdapter(null);
        mAdapter.setChannel(mChannel);
        mWorldUsers = new TreeSet<>(new IRCUserComparator(mChannel));
        mWorldUsers.addAll(userList);
        mAdapter.setInternalSet(mWorldUsers);
        getListView().setAdapter(mAdapter);

        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public void onPanelOpened() {
        onUpdateUserList();
        onStartObserving();
    }

    boolean isNickOtherUsers(final String nick) {
        return !mChannel.getServer().getUser().getNick().equals(nick);
    }

    public void onPanelClosed() {
        onStopObserving();

        // The list view is null when we are coming into a rotation and this method is called
        getListView().setAdapter(null);

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
    public void onEventMainThread(final WorldUserEvent event) {
        if (event.channelName.equals(mChannel.getName())) {
            onUpdateUserList();
        }
    }
    // End of subscribed events

    public void onStartObserving() {
        mChannel.getServer().getServerEventBus().register(this);
    }

    public void onStopObserving() {
        mChannel.getServer().getServerEventBus().unregister(this);
    }

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

    protected List<WorldUser> getCheckedItems() {
        final List<WorldUser> checkedSessionPositions = new ArrayList<>();
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
        final List<WorldUser> selectedItems = getCheckedItems();
        final String nick = selectedItems.get(0).getNick();
        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mCallback.onUserMention(selectedItems);
                mode.finish();
                mCallback.closeDrawer();
                return true;
            case R.id.fragment_userlist_cab_pm: {
                if (isNickOtherUsers(nick)) {
                    mChannel.getServer().getServerCallBus()
                            .sendMessageToUser(nick, "");
                    mCallback.closeDrawer();
                    mode.finish();
                } else {
                    final AlertDialog.Builder build = new AlertDialog.Builder(
                            getActivity());
                    build.setTitle(getActivity()
                            .getString(R.string.user_list_not_possible))
                            .setMessage(getActivity()
                                    .getString(R.string.user_list_pm_self_not_possible))
                            .setPositiveButton(getActivity().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialogInterface,
                                                int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }
                            );
                    build.show();
                }
                return true;
            }
            case R.id.fragment_userlist_cab_whois:
                mChannel.getServer().getServerCallBus().sendUserWhois(nick);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    public interface Callback {

        public void onUserMention(final List<WorldUser> users);

        public void closeDrawer();
    }
}