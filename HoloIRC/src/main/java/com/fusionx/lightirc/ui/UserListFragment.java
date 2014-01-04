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
import com.fusionx.lightirc.util.MultiSelectionUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.channel.WorldUserEvent;
import com.fusionx.relay.misc.IRCUserComparator;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class UserListFragment extends MultiChoiceStickyListFragment<WorldUser> implements
        SlidingMenu.OnCloseListener {

    private Callbacks mCallback;

    private Channel mChannel;

    private UserListAdapter mAdapter;

    private TreeSet<WorldUser> worldUsers;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        worldUsers = new TreeSet<>(new IRCUserComparator(mChannel));
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

        mAdapter = new UserListAdapter(view.getContext(), worldUsers);
        getListView().setAdapter(mAdapter);

        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCallback.isUserSlidingMenuOpen()) {
            onStartObserving();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCallback.isUserSlidingMenuOpen()) {
            onStopObserving();
        }
    }

    @Override
    protected void attachSelectionController() {
        mMultiSelectionController = MultiSelectionUtils.attachMultiSelectionController(
                getListView().getWrappedList(), (ActionBarActivity) getActivity(), this, true);
    }

    public void onMenuOpened(final Channel channel) {
        mChannel = channel;

        final Collection<WorldUser> userList = channel.getUsers();

        worldUsers = new TreeSet<>(new IRCUserComparator(mChannel));
        worldUsers.addAll(userList);

        mAdapter.setInternalSet(worldUsers);
        mAdapter.setChannel(channel);
        getListView().setAdapter(mAdapter);

        onStartObserving();
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final List<WorldUser> selectedItems = getCheckedItems();
        final String nick = selectedItems.get(0).getNick();
        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mCallback.onUserMention(selectedItems);
                mode.finish();
                mCallback.closeAllSlidingMenus();
                return true;
            case R.id.fragment_userlist_cab_pm: {
                if (isNickOtherUsers(nick)) {
                    mCallback.getServer().getServerCallBus().sendMessageToUser(nick, "");
                    mCallback.closeAllSlidingMenus();
                    mode.finish();
                } else {
                    final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
                    build.setTitle(getActivity()
                            .getString(R.string.user_list_not_possible)).setMessage(getActivity()
                            .getString(R.string.user_list_pm_self_not_possible))
                            .setPositiveButton(getActivity().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                    build.show();
                }
                return true;
            }
            case R.id.fragment_userlist_cab_whois:
                mCallback.getServer().getServerCallBus().sendUserWhois(nick);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        final MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.fragment_userlist_cab, menu);
        return true;
    }

    boolean isNickOtherUsers(final String nick) {
        return !mCallback.getServer().getUser().getNick().equals(nick);
    }

    @Override
    public void onClose() {
        onStopObserving();

        getListView().setAdapter(null);

        if (mMultiSelectionController != null) {
            mMultiSelectionController.finish();
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        int selectedItemCount = getCheckedItems().size();

        if (selectedItemCount != 0) {
            final String quantityString = getResources().getQuantityString(R.plurals.user_selection,
                    selectedItemCount, selectedItemCount);
            mode.setTitle(quantityString);

            mode.getMenu().getItem(1).setVisible(selectedItemCount == 1);
            mode.getMenu().getItem(2).setVisible(selectedItemCount == 1);
        }
    }

    @Override
    protected UserListAdapter getRealAdapter() {
        return mAdapter;
    }

    /*
     * Subscribed events
     *
     * Only perform an action if the channel that is being observed currently is the one the
     * event is referring to
     */
    /*@Subscribe
    public void onUserListChanged(final ChannelEvent event) {
        if (isChannelEqual(event.channelName)) {
            switch (event.changeType) {
                case ADD:
                    mAdapter.add(event.user);
                    break;
                case REMOVE:
                    mAdapter.remove(event.user);
                    break;
                case MODIFIED:
                    // Someone's nick has changed
                    mAdapter.notifyDataSetChanged();
                    break;
            }
            if (mMultiSelectionController != null) {
                mMultiSelectionController.finish();
            }
        }
    }*/
    @Subscribe
    public void onWorldUserEvent(final WorldUserEvent event) {
        if (event.channelName.equals(mChannel.getName())) {
            final Collection<WorldUser> userList = mChannel.getUsers();

            getListView().setAdapter(null);
            worldUsers.clear();
            worldUsers.addAll(userList);
            getListView().setAdapter(mAdapter);

            if (mMultiSelectionController != null) {
                mMultiSelectionController.finish();
            }
        }
    }
    // End of subscribed events

    public void onStartObserving() {
        mCallback.getServer().getServerEventBus().register(this);
    }

    public void onStopObserving() {
        mCallback.getServer().getServerEventBus().unregister(this);
    }

    public interface Callbacks {

        public void onUserMention(final List<WorldUser> users);

        public Server getServer();

        public void closeAllSlidingMenus();

        public boolean isUserSlidingMenuOpen();
    }
}