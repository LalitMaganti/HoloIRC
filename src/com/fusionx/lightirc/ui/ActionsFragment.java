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
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ActionsAdapter;
import com.fusionx.lightirc.communication.ServerCommandSender;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.ui.dialogbuilder.ChannelNamePromptDialogBuilder;
import com.fusionx.lightirc.ui.dialogbuilder.NickPromptDialogBuilder;
import com.fusionx.lightirc.util.FragmentUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class ActionsFragment extends ListFragment implements AdapterView.OnItemClickListener,
        SlidingMenu.OnOpenListener {
    private IRCActionsCallback callback;
    private FragmentTypeEnum type;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        callback = FragmentUtils.getParent(this, IRCActionsCallback.class);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setListAdapter(new ActionsAdapter(getActivity()));
        if (type != null) {
            getListAdapter().setFragmentType(type);
            type = null;
        }
        return inflater.inflate(R.layout.fragment_action_listview, container, false);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
                            final long l) {
        switch (i) {
            case 0:
                channelNameDialog();
                break;
            case 1:
                nickChangeDialog();
                break;
            case 2:
                final Server server = callback.getServer(true);
                if (server == null) {
                    callback.onDisconnect(true, false);
                } else {
                    ServerCommandSender.sendDisconnect(server, getActivity());
                }
                return;
            case 3:
                ActionsPagerFragment fragment = (ActionsPagerFragment) getParentFragment();
                fragment.switchToIgnoreFragment();
                return;
            case 4:
                callback.closeOrPartCurrentTab();
                break;
        }
        callback.closeAllSlidingMenus();
    }

    private void nickChangeDialog() {
        final NickPromptDialogBuilder nickDialog = new NickPromptDialogBuilder(getActivity(),
                callback.getNick()) {
            @Override
            public void onOkClicked(final String input) {
                ServerCommandSender.sendNickChange(callback.getServer(false), input);
            }
        };
        nickDialog.show();
    }

    private void channelNameDialog() {
        final ChannelNamePromptDialogBuilder builder = new ChannelNamePromptDialogBuilder
                (getActivity()) {
            @Override
            public void onOkClicked(final String input) {
                ServerCommandSender.sendJoin(callback.getServer(false), input);
            }
        };
        builder.show();
    }

    @Override
    public void onOpen() {
        if (callback.isConnectedToServer() != getListAdapter().isConnected()) {
            getListAdapter().setConnected(callback.isConnectedToServer());
            getListAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public ActionsAdapter getListAdapter() {
        return (ActionsAdapter) super.getListAdapter();
    }

    public void updateConnectionStatus(final boolean isConnected) {
        getListAdapter().setConnected(isConnected);
        getListAdapter().notifyDataSetChanged();
    }

    public void onTabChanged(final FragmentTypeEnum selectedType) {
        if (getListAdapter() == null) {
            type = selectedType;
        } else {
            getListAdapter().setFragmentType(selectedType);
        }
    }

    @Override
    public StickyListHeadersListView getListView() {
        return (StickyListHeadersListView) super.getListView();
    }

    public interface IRCActionsCallback {
        public String getNick();

        public void closeOrPartCurrentTab();

        public boolean isConnectedToServer();

        public Server getServer(boolean nullable);

        public void closeAllSlidingMenus();

        public void onDisconnect(boolean expected, boolean retryPending);
    }
}