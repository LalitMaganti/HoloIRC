/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.ActionsArrayAdapter;
import com.fusionx.lightirc.interfaces.CommonIRCListenerInterface;
import com.fusionx.lightirc.promptdialogs.ChannelNamePromptDialogBuilder;
import com.fusionx.lightirc.promptdialogs.NickPromptDialogBuilder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class IRCActionsFragment extends ListFragment implements AdapterView.OnItemClickListener,
        SlidingMenu.OnOpenListener {
    private IRCActionsListenerInterface mListener;
    private CommonIRCListenerInterface mCommonListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IRCActionsListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IRCActionsListenerInterface");
        }
        try {
            mCommonListener = (CommonIRCListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CommonIRCListenerInterface");
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String[] values = getResources().getStringArray(R.array.actions);
        final ActionsArrayAdapter adapter = new ActionsArrayAdapter(getActivity(),
                values);
        setListAdapter(adapter);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        mCommonListener.closeAllSlidingMenus();

        switch (i) {
            case 0:
                channelNameDialog();
                break;
            case 1:
                nickChangeDialog();
                break;
            case 2:
                connectionStatusChanged(false);
                mListener.disconnect();
                break;
        }
    }

    public void connectionStatusChanged(boolean connected) {
        ((ActionsArrayAdapter) getListAdapter()).setConnected(connected);
        ((ActionsArrayAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void nickChangeDialog() {
        final NickPromptDialogBuilder nickDialog = new NickPromptDialogBuilder(getActivity(), mListener.getNick()) {
            @Override
            public void onOkClicked(final String input) {
                final AsyncTask<Void, Void, Void> ChangeNickTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... objects) {
                        mListener.changeNick(input);
                        return null;
                    }
                };
                ChangeNickTask.execute();
            }
        };
        nickDialog.show();
    }

    private void channelNameDialog() {
        final ChannelNamePromptDialogBuilder builder = new ChannelNamePromptDialogBuilder(getActivity()) {
            @Override
            public void onOkClicked(final String input) {
                final AsyncTask<Void, Void, Void> JoinTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... objects) {
                        mListener.joinChannel(input);
                        return null;
                    }
                };
                JoinTask.execute();
            }
        };
        builder.show();
    }

    @Override
    public void onOpen() {
        final ActionsArrayAdapter arrayAdapter = (ActionsArrayAdapter) getListView().getAdapter();
        arrayAdapter.setConnected(mCommonListener.isConnectedToServer());
        arrayAdapter.notifyDataSetChanged();
    }

    public interface IRCActionsListenerInterface {
        public String getNick();

        public void joinChannel(final String channel);

        public void changeNick(final String newNick);

        public void disconnect();
    }
}