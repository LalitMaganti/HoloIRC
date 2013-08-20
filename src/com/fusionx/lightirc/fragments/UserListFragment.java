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

package com.fusionx.lightirc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.fusionx.irc.ChannelUser;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.UserListAdapter;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.uiircinterface.ServerCommandSender;

import java.util.ArrayList;
import java.util.TreeSet;

import lombok.Getter;

public class UserListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener,
        AdapterView.OnItemClickListener {
    @Getter
    private ActionMode mode;
    private UserListCallback mListener;
    private String currentlyDisplayedChannelName;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (UserListCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserListCallback");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final UserListAdapter adapter = new UserListAdapter(inflater.getContext(),
                new TreeSet<ChannelUser>());
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onMenuOpened(final String channelName) {
        if (!channelName.equals(currentlyDisplayedChannelName)) {
            final TreeSet<ChannelUser> userList = getUserList(channelName);
            if (userList != null) {
                getListAdapter().setInternalSet(userList);
                getListAdapter().setChannelName(channelName);
                currentlyDisplayedChannelName = channelName;
            }
        }
        getListView().smoothScrollToPosition(0);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                                          final boolean checked) {
        mode.invalidate();

        if (checked) {
            getListAdapter().addSelection(position);
        } else {
            getListAdapter().removeSelection(position);
        }

        int selectedItemCount = getListView().getCheckedItemCount();

        if (selectedItemCount != 0) {
            final String quantityString = getResources().getQuantityString(R.plurals.user_selection,
                    selectedItemCount, selectedItemCount);

            mode.setTitle(quantityString);

            mode.getMenu().getItem(1).setVisible(selectedItemCount == 1);
            mode.getMenu().getItem(2).setVisible(selectedItemCount == 1);
        }
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        final ArrayList<ChannelUser> selectedItems = getListAdapter().getSelectedItems();
        final String nick = selectedItems.get(0).getNick();
        switch (item.getItemId()) {
            case R.id.fragment_userlist_cab_mention:
                mListener.onUserMention(selectedItems);
                mode.finish();
                mListener.closeAllSlidingMenus();
                return true;
            case R.id.fragment_userlist_cab_pm: {
                if (isNickOtherUsers(nick)) {
                    ServerCommandSender.sendMessageToUser(mListener.getServer(false), nick, "");
                    mListener.closeAllSlidingMenus();
                    mode.finish();
                } else {
                    final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
                    build.setTitle(getActivity()
                            .getString(R.string.user_list_not_possible))
                            .setMessage(getActivity()
                                    .getString(R.string.user_list_pm_self_not_possible))
                            .setPositiveButton(getActivity()
                                    .getString(R.string.ok),
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
                mListener.selectServerFragment();
                ServerCommandSender.sendUserWhois(mListener.getServer(false), nick);
                mode.finish();
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
        getListAdapter().clearSelection();

        this.mode = null;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        return false;
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
                            final long l) {
        if (mode == null) {
            getActivity().startActionMode(this);

            final boolean checked = getListAdapter().getSelectedItems()
                    .contains(getListAdapter().getItem(i));
            getListView().setItemChecked(i, !checked);
        }
    }

    @Override
    public UserListAdapter getListAdapter() {
        return (UserListAdapter) super.getListAdapter();
    }

    public void updateUserList() {
        if (mode != null) {
            mode.finish();
        }
        getListAdapter().notifyDataSetChanged();
    }

    public TreeSet<ChannelUser> getUserList(final String channelName) {
        return mListener.getServer(false).getUserChannelInterface()
                .getChannel(channelName).getUsers();
    }

    public boolean isNickOtherUsers(final String nick) {
        return !mListener.getServer(false).getUser().getNick().equals(nick);
    }

    public interface UserListCallback extends CommonCallbacks {
        public void onUserMention(final ArrayList<ChannelUser> users);

        public void createPMFragment(final String userNick);
    }
}