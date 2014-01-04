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

import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerStatus;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.ServerEvent;
import com.fusionx.relay.parser.UserInputParser;
import com.squareup.otto.Subscribe;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class ServerFragment extends IRCFragment<ServerEvent> {

    private Callbacks mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, Callbacks.class);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);

        mMessageBox.setEnabled(mCallback.isConnectedToServer());
    }

    @Override
    public void onResume() {
        super.onResume();

        getServer().getServerEventBus().register(this);

        if (getServer().getStatus() != ServerStatus.CONNECTED) {
            onDisableUserInput();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getServer().getServerEventBus().unregister(this);
    }

    @Override
    protected List<ServerEvent> onRetrieveMessages() {
        final Server server = getServer();
        if (server != null) {
            return new ArrayList<>(server.getBuffer());
        } else {
            return null;
        }
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseServerMessage(getServer(), message);
    }

    public void onConnected() {
        mMessageBox.setEnabled(true);
    }

    // Subscription event
    @Subscribe
    public void onServerEvent(final ServerEvent event) {
        if (!(event instanceof JoinEvent) && !(event instanceof PartEvent)) {
            mMessageAdapter.add(event);
        }
    }

    public Server getServer() {
        return mCallback.getServer();
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Server;
    }

    public interface Callbacks {

        public Server getServer();

        public boolean isConnectedToServer();
    }
}