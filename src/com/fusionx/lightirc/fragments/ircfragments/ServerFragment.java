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

package com.fusionx.lightirc.fragments.ircfragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.fusionx.irc.Server;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.uiircinterface.MessageParser;

import lombok.Getter;

public class ServerFragment extends IRCFragment {
    private ServerFragmentCallback mCallback;

    @Getter
    private final Handler serverFragHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerEventType type = (ServerEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            final String message = bundle.getString(EventBundleKeys.message);
            switch (type) {
                case Connected:
                    mCallback.connectedToServer();
                    mEditText.setEnabled(true);
                    break;
                case Disconnected:
                case Error:
                    mCallback.onUnexpectedDisconnect();
                    break;
                case NickInUse:
                    mCallback.selectServerFragment();
                    break;
            }
            appendToTextView(message + "\n");
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        mEditText.setEnabled(mCallback.isConnectedToServer());

        final Server server = mCallback.getServer(true);
        if (server != null) {
            writeToTextView(server.getBuffer());
        }
    }

    @Override
    public void sendMessage(String message) {
        MessageParser.serverMessageToParse(mCallback, message);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (ServerFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement ServerFragmentCallback");
        }
    }

    public interface ServerFragmentCallback extends CommonCallbacks {
        public void connectedToServer();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.Server;
    }

    @Override
    public Handler getHandler() {
        return serverFragHandler;
    }
}