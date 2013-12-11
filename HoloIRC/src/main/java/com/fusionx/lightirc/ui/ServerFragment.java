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

import com.fusionx.relay.Message;
import com.fusionx.relay.Server;
import com.fusionx.relay.parser.UserInputParser;
import com.fusionx.relay.event.ServerEvent;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.util.FragmentUtils;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class ServerFragment extends IRCFragment {

    private ServerFragmentCallback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, ServerFragmentCallback.class);
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

        mCallback.getServer().getServerEventBus().register(this);
        mCallback.getServer().getServerCache().setCached(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCallback.getServer().getServerEventBus().unregister(this);
        mCallback.getServer().getServerCache().setCached(false);
    }

    @Override
    protected List<Message> onRetrieveMessages() {
        if(mCallback.getServer() != null) {
            return mCallback.getServer().getBuffer();
        } else {
            return null;
        }
    }

    @Override
    public void onSendMessage(final String message) {
        final Server server = mCallback.getServer();
        UserInputParser.serverMessageToParse(server, message, mCallback);
    }

    public void onConnected() {
        mMessageBox.setEnabled(true);
    }

    // Subscription event
    @Subscribe
    public void onServerEvent(final ServerEvent event) {
        if (StringUtils.isNotBlank(event.message)) {
            if(mMessageAdapter == null) {
                setupListAdapter();
            }
            synchronized (mMessageAdapter.getMessages()) {
                mMessageAdapter.add(new Message(event.message));
            }
        }
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Server;
    }

    public interface ServerFragmentCallback extends UserInputParser.ParserCallbacks {

        public Server getServer();

        public boolean isConnectedToServer();
    }
}