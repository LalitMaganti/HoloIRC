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
import com.fusionx.lightirc.adapters.ActionsAdapter;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.ui.dialogbuilder.DialogBuilder;
import com.fusionx.lightirc.ui.dialogbuilder.NickDialogBuilder;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.interfaces.Conversation;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.fusionx.lightirc.util.MiscUtils.getBus;
import static com.fusionx.lightirc.util.UIUtils.findById;

public class ActionsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private Conversation mConversation;

    private final Object mEventHandler = new Object() {
        @Subscribe
        public void onEvent(final OnConversationChanged conversationChanged) {
            mConversation = conversationChanged.conversation;
        }
    };

    private Callbacks mCallbacks;

    private ActionsAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.default_stickylist_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBus().registerSticky(mEventHandler);

        mAdapter = new ActionsAdapter(getActivity());
        final StickyListHeadersListView listView = findById(view, android.R.id.list);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventHandler);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
            final long l) {
        final String action = mAdapter.getItem(i);

        if (action.equals(getString(R.string.action_join_channel))) {
            showChannelDialog();
        } else if (action.equals(getString(R.string.action_change_nick))) {
            showNickDialog();
        } else if (action.equals(getString(R.string.action_ignore_list))) {
            mCallbacks.switchToIgnoreFragment();
            return;
        } else if (action.equals(getString(R.string.action_pending_invites))) {
            mCallbacks.switchToInviteFragment();
            return;
        } else if (action.equals(getString(R.string.action_disconnect))) {
            mCallbacks.disconnectFromServer();
        } else if (action.equals(getString(R.string.action_close_server))) {
            mCallbacks.disconnectFromServer();
        } else if (action.equals(getString(R.string.action_reconnect))) {
            mCallbacks.reconnectToServer();
        } else if (action.equals(getString(R.string.action_part_channel))) {
            mCallbacks.removeCurrentFragment();
        } else if (action.equals(getString(R.string.action_close_pm))) {
            mCallbacks.removeCurrentFragment();
        }

        mCallbacks.closeDrawer();
    }

    private void showNickDialog() {
        final NickDialogBuilder nickDialog = new NickDialogBuilder(getActivity(),
                mConversation.getServer().getUser().getNick().getNickAsString()) {
            @Override
            public void onOkClicked(final String input) {
                mConversation.getServer().getServerCallBus().sendNickChange(input);
            }
        };
        nickDialog.show();
    }

    private void showChannelDialog() {
        final ChannelDialogBuilder builder = new ChannelDialogBuilder();
        builder.show();
    }

    public interface Callbacks {

        public void removeCurrentFragment();

        public void closeDrawer();

        public void disconnectFromServer();

        public void reconnectToServer();

        public void switchToIgnoreFragment();

        public void switchToInviteFragment();
    }

    public class ChannelDialogBuilder extends DialogBuilder {

        public ChannelDialogBuilder() {
            super(getActivity(), getActivity().getString(R.string.prompt_dialog_channel_name),
                    getActivity().getString(R.string.prompt_dialog_including_starting), "");
        }

        @Override
        public void onOkClicked(final String input) {
            mConversation.getServer().getServerCallBus().sendJoin(input);
        }
    }
}