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
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.actions.ServerActionsAdapter;
import com.fusionx.lightirc.adapters.actions.UserChannelActionsAdapter;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.promptdialogs.ChannelNamePromptDialogBuilder;
import com.fusionx.lightirc.promptdialogs.NickPromptDialogBuilder;
import com.fusionx.uiircinterface.ServerCommandSender;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class IRCActionsFragment extends ListFragment implements AdapterView.OnItemClickListener,
        SlidingMenu.OnOpenListener {
    private IRCActionsCallback mListener;
    private FragmentType type;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IRCActionsCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement IRCActionsCallback");
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final MergeAdapter mergeAdapter = new MergeAdapter();
        final ServerActionsAdapter adapter = new ServerActionsAdapter(getActivity(),
                getResources().getStringArray(R.array.server_actions));
        final UserChannelActionsAdapter channelAdapter = new UserChannelActionsAdapter
                (getActivity());
        final View serverHeader = inflater.inflate(R.layout.sliding_menu_header, null);
        TextView textView = (TextView) serverHeader.findViewById(R.id
                .sliding_menu_heading_textview);
        textView.setText("Server");
        final View otherHeader = inflater.inflate(R.layout.sliding_menu_header, null);

        mergeAdapter.addView(serverHeader);
        mergeAdapter.addAdapter(adapter);
        mergeAdapter.addView(otherHeader);
        mergeAdapter.addAdapter(channelAdapter);

        setListAdapter(mergeAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i,
                            final long l) {
        switch (i) {
            case 1:
                channelNameDialog();
                break;
            case 2:
                nickChangeDialog();
                break;
            case 3:
                mListener.disconnect();
                return;
            case 5:
                mListener.closeOrPartCurrentTab();
                break;
        }
        mListener.closeAllSlidingMenus();
    }

    private void nickChangeDialog() {
        final NickPromptDialogBuilder nickDialog = new NickPromptDialogBuilder(getActivity(),
                mListener.getNick()) {
            @Override
            public void onOkClicked(final String input) {
                ServerCommandSender.sendNickChange(mListener.getServer(false), input);
            }
        };
        nickDialog.show();
    }

    private void channelNameDialog() {
        final ChannelNamePromptDialogBuilder builder = new ChannelNamePromptDialogBuilder
                (getActivity()) {
            @Override
            public void onOkClicked(final String input) {
                ServerCommandSender.sendJoin(mListener.getServer(false), input);
            }
        };
        builder.show();
    }

    @Override
    public void onOpen() {
        if (mListener.isConnectedToServer() != getServerAdapter().isConnected()) {
            getServerAdapter().setConnected(mListener.isConnectedToServer());
            getServerAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public MergeAdapter getListAdapter() {
        return (MergeAdapter) super.getListAdapter();
    }

    public ServerActionsAdapter getServerAdapter() {
        return (ServerActionsAdapter) getListAdapter().getAdapter(1);
    }

    public UserChannelActionsAdapter getUserChannelAdapter() {
        return (UserChannelActionsAdapter) getListAdapter().getAdapter(3);
    }

    public void updateConnectionStatus() {
        getServerAdapter().setConnected(mListener.isConnectedToServer());
        getServerAdapter().notifyDataSetChanged();
    }

    public void onTabChanged() {
        if (mListener.getCurrentlyDisplayedFragment() != type) {
            type = mListener.getCurrentlyDisplayedFragment();
            final View view = (View) getListAdapter().getItem(4);
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

    public interface IRCActionsCallback extends CommonCallbacks {
        public String getNick();

        public void closeOrPartCurrentTab();

        public FragmentType getCurrentlyDisplayedFragment();
    }
}