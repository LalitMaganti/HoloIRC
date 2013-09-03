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

package com.fusionx.lightirc.fragments.actions;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.fusionx.common.utils.FragmentUtil;
import com.fusionx.irc.core.Server;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.actions.ServerActionsAdapter;
import com.fusionx.lightirc.adapters.actions.UserChannelActionsAdapter;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.promptdialogs.ChannelNamePromptDialogBuilder;
import com.fusionx.lightirc.promptdialogs.NickPromptDialogBuilder;
import com.fusionx.uiircinterface.core.ServerCommandSender;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.Arrays;

public class IRCActionsFragment extends ListFragment implements AdapterView.OnItemClickListener,
        SlidingMenu.OnOpenListener {
    private FragmentType type;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final MergeAdapter mergeAdapter = new MergeAdapter();
        final ServerActionsAdapter adapter = new ServerActionsAdapter(getActivity(),
                Arrays.asList(getResources().getStringArray(R.array.server_actions)));
        final UserChannelActionsAdapter channelAdapter = new UserChannelActionsAdapter
                (getActivity());

        final View serverHeader = inflater.inflate(R.layout.sliding_menu_header, null);
        final TextView textView = (TextView) serverHeader.findViewById(R.id
                .sliding_menu_heading_textview);
        textView.setText(getActivity().getString(R.string.server));

        final View otherHeader = inflater.inflate(R.layout.sliding_menu_header, null);
        final TextView otherTextView = (TextView) otherHeader.findViewById(R.id
                .sliding_menu_heading_textview);

        if (type == null || type.equals(FragmentType.Server)) {
            otherHeader.setVisibility(View.GONE);
            channelAdapter.setServerVisible();
        } else if (type.equals(FragmentType.Channel)) {
            otherTextView.setText(getActivity().getString(R.string.channel));
        } else {
            otherTextView.setText(getActivity().getString(R.string.user));
        }

        mergeAdapter.addView(serverHeader);
        mergeAdapter.addAdapter(adapter);
        mergeAdapter.addView(otherHeader);
        mergeAdapter.addAdapter(channelAdapter);

        final AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter
                (mergeAdapter);
        setListAdapter(alphaInAnimationAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getAlphaAdapter().setAbsListView(getListView());
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
                            final long l) {
        final IRCActionsCallback callback = FragmentUtil.getParent(this, IRCActionsCallback.class);

        switch (i) {
            case 1:
                channelNameDialog();
                break;
            case 2:
                nickChangeDialog();
                break;
            case 3:
                final Server server = callback.getServer(true);
                if (server == null) {
                    callback.onDisconnect(true, false);
                } else {
                    ServerCommandSender.sendDisconnect(server, getActivity());
                }
                return;
            case 4:
                ActionsPagerFragment fragment = (ActionsPagerFragment) getParentFragment();
                fragment.switchToIgnoreFragment();
                return;
            case 6:
                callback.closeOrPartCurrentTab();
                break;
        }
        callback.closeAllSlidingMenus();
    }

    private void nickChangeDialog() {
        final IRCActionsCallback callback = FragmentUtil.getParent(this, IRCActionsCallback.class);
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
        final IRCActionsCallback callback = FragmentUtil.getParent(this, IRCActionsCallback.class);
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
        final IRCActionsCallback callback = FragmentUtil.getParent(this, IRCActionsCallback.class);
        if (callback.isConnectedToServer() != getServerAdapter().isConnected()) {
            getServerAdapter().setConnected(callback.isConnectedToServer());
            getServerAdapter().notifyDataSetChanged();
        }
    }

    public AlphaInAnimationAdapter getAlphaAdapter() {
        return (AlphaInAnimationAdapter) super.getListAdapter();
    }

    @Override
    public MergeAdapter getListAdapter() {
        return (MergeAdapter) getAlphaAdapter().getDecoratedBaseAdapter();
    }

    private ServerActionsAdapter getServerAdapter() {
        return (ServerActionsAdapter) getListAdapter().getAdapter(1);
    }

    private UserChannelActionsAdapter getUserChannelAdapter() {
        return (UserChannelActionsAdapter) getListAdapter().getAdapter(3);
    }

    public void updateConnectionStatus(final boolean isConnected) {
        getServerAdapter().setConnected(isConnected);
        getServerAdapter().notifyDataSetChanged();
    }

    public void onTabChanged(final FragmentType selectedType) {
        if (selectedType != type) {
            type = selectedType;
            if (getAlphaAdapter() != null && getListAdapter() != null) {
                final View view = (View) getListAdapter().getItem(5);
                final TextView textView = (TextView) view.findViewById(R.id
                        .sliding_menu_heading_textview);
                switch (type) {
                    case Server:
                        view.setVisibility(View.GONE);
                        getUserChannelAdapter().setServerVisible();
                        break;
                    case Channel:
                        view.setVisibility(View.VISIBLE);
                        textView.setText(getActivity().getString(R.string.channel));
                        getUserChannelAdapter().setChannelVisible(true);
                        break;
                    case User:
                        view.setVisibility(View.VISIBLE);
                        textView.setText(getActivity().getString(R.string.user));
                        getUserChannelAdapter().setChannelVisible(false);
                        break;
                }
                getUserChannelAdapter().notifyDataSetChanged();
            }
        }
    }

    public interface IRCActionsCallback {
        public String getNick();

        public void closeOrPartCurrentTab();

        public boolean isConnectedToServer();

        public Server getServer(boolean nullable);

        public void closeAllSlidingMenus();

        public void onDisconnect(boolean expected, boolean retryPending);

        public void switchToIRCActionFragment();
    }
}