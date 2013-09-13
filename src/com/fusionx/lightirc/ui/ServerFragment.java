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

import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;

import com.fusionx.lightirc.adapters.IRCAnimationAdapter;
import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.communication.MessageParser;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.util.FragmentUtils;

import java.util.ArrayList;

public class ServerFragment extends IRCFragment {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);

        final ServerFragmentCallback callback = FragmentUtils.getParent(ServerFragment.this,
                ServerFragmentCallback.class);
        mEditText.setEnabled(callback.isConnectedToServer());
    }

    @Override
    public void onResume() {
        super.onResume();

        final ServerFragmentCallback callback = FragmentUtils.getParent(this,
                ServerFragmentCallback.class);
        final Server server = callback.getServer(true);
        if (server != null && getListAdapter() == null) {
            final IRCMessageAdapter adapter = server.getBuffer() == null ? new IRCMessageAdapter
                    (getActivity(), new ArrayList<Spanned>()) : server.getBuffer();
            final IRCAnimationAdapter alphaInAnimationAdapter = new IRCAnimationAdapter
                    (adapter);
            alphaInAnimationAdapter.setAbsListView(getListView());
            setListAdapter(alphaInAnimationAdapter);
            server.setBuffer(adapter);
            getListView().setSelection(getListView().getCount());
        }
    }

    @Override
    public void sendMessage(String message) {
        final ServerFragmentCallback callback = FragmentUtils.getParent(ServerFragment.this,
                ServerFragmentCallback.class);
        final Server server = callback.getServer(true);
        MessageParser.serverMessageToParse(getActivity(), server, message);
    }

    public void onConnectedToServer() {
        mEditText.setEnabled(true);
    }

    public interface ServerFragmentCallback {
        public Server getServer(boolean nullable);

        public boolean isConnectedToServer();
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Server;
    }
}