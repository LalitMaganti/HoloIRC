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
import android.os.Handler;
import android.os.Message;

import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.constants.EventBundleKeys;
import com.fusionx.lightirc.constants.ServerEventTypeEnum;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.uiircinterface.MessageParser;
import com.fusionx.lightirc.util.FragmentUtils;

import lombok.Getter;

public class ServerFragment extends IRCFragment {
    @Getter
    private final Handler serverFragHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerEventTypeEnum type = (ServerEventTypeEnum) bundle.getSerializable(EventBundleKeys
                    .eventType);
            final String message = bundle.getString(EventBundleKeys.message);
            final ServerFragmentCallback callback = FragmentUtils.getParent(ServerFragment.this,
                    ServerFragmentCallback.class);
            switch (type) {
                case NickInUse:
                    callback.selectServerFragment();
                    break;
            }
            appendToTextView(message + "\n");
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        final ServerFragmentCallback callback = FragmentUtils.getParent(ServerFragment.this,
                ServerFragmentCallback.class);
        final Server server = callback.getServer(true);
        mEditText.setEnabled(server != null && server.isConnected(getActivity()));

        if (server != null) {
            writeToTextView(server.getBuffer());
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

        public void selectServerFragment();
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Server;
    }

    @Override
    public Handler getHandler() {
        return serverFragHandler;
    }
}