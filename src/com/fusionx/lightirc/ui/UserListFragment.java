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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.collections.UserListTreeSet;
import com.fusionx.lightirc.communication.ServerCommandSender;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.Server;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.TreeSet;

public class UserListFragment extends ListFragment implements AdapterView.OnItemClickListener,
        ActionMode.Callback, AdapterView.OnItemLongClickListener, SlidingMenu.OnCloseListener {
    private ActionMode mode;
    private UserListCallback mCallback;
    private String mChannelName;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (UserListCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UserListCallback");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final UserListAdapter adapter = new UserListAdapter(view.getContext(),
                new TreeSet<ChannelUser>());
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        setListAdapter(alphaInAnimationAdapter);
        alphaInAnimationAdapter.setAbsListView(getListView());
    }

    public void onMenuOpened(final String channelName) {
        if (!channelName.equals(mChannelName)) {
            final UserListTreeSet userList = getUserList(channelName);
            if (userList != null) {
                mChannelName = channelName;
                getUserListAdapter().setInternalSet(userList);
                getUserListAdapter().setChannelName(channelName);
                getListAdapter().notifyDataSetChanged();
            } else {
                getUserListAdapter().clear();
            }
        }
        getListView().smoothScrollToPosition(0);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final ArrayList<ChannelUser> selectedItems = getUserListAdapter().getSelectedItems();
        final String nick = selectedItems.get(0).getNick();
        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mCallback.onUserMention(selectedItems);
                mode.finish();
                mCallback.closeAllSlidingMenus();
                return true;
            case R.id.fragment_userlist_cab_pm: {
                if (isNickOtherUsers(nick)) {
                    ServerCommandSender.sendMessageToUser(mCallback.getServer(false), nick, "");
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
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                    build.show();
                }
                return true;
            }
            case R.id.fragment_userlist_cab_whois:
                ServerCommandSender.sendUserWhois(mCallback.getServer(false), nick);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        final MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.fragment_userlist_cab, menu);

        this.mode = mode;

        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        getUserListAdapter().clearSelection();

        this.mode = null;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        int selectedItemCount = getUserListAdapter().getSelectedItemCount();

        if (selectedItemCount == 0) {
            mode.finish();
        } else {
            final String quantityString = getResources().getQuantityString(R.plurals.user_selection,
                    selectedItemCount, selectedItemCount);

            mode.setTitle(quantityString);

            mode.getMenu().getItem(1).setVisible(selectedItemCount == 1);
            mode.getMenu().getItem(2).setVisible(selectedItemCount == 1);
        }

        return true;
    }

    public UserListAdapter getUserListAdapter() {
        return (UserListAdapter) getListAdapter().getDecoratedBaseAdapter();
    }

    @Override
    public AlphaInAnimationAdapter getListAdapter() {
        return (AlphaInAnimationAdapter) super.getListAdapter();
    }

    public void onUserListUpdated() {
        if (mode != null) {
            mode.finish();
        }
        getListAdapter().notifyDataSetChanged();
    }

    public UserListTreeSet getUserList(final String channelName) {
        return mCallback.getServer(false).getUserChannelInterface().getChannel(channelName)
                .getUsers();
    }

    public boolean isNickOtherUsers(final String nick) {
        return !mCallback.getServer(false).getUser().getNick().equals(nick);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
                            final long l) {
        if (mode == null) {
            ((ActionBarActivity) getActivity()).startSupportActionMode(this);
        }
        getUserListAdapter().toggleSelection(i);
        mode.invalidate();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        onItemClick(adapterView, view, i, l);
        return true;
    }

    @Override
    public void onClose() {
        if (mode != null) {
            mode.finish();
        }
    }

    public void part() {
        mChannelName = "null";
        getUserListAdapter().clear();
    }

    public interface UserListCallback {
        public void onUserMention(final ArrayList<ChannelUser> users);

        public void createPMFragment(final String userNick);

        public Server getServer(boolean nullable);

        public void closeAllSlidingMenus();
    }
}