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
import android.os.Message;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fusionx.irc.Server;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.lightirc.handlerabstract.ServerFragHandler;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.parser.MessageParser;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

public class ServerFragment extends IRCFragment {
    private ServerFragmentCallback mCallback;

    @Override
    public void onResume() {
        super.onResume();

        MessageSender.getSender(mCallback.getServerTitle()).registerServerFragmentHandler
                (mServerHandler);

        editText.setEnabled(mCallback.isConnectedToServer());

        final Server server = mCallback.getServer(true);
        if (server != null) {
            writeToTextView(server.getBuffer());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        MessageSender.getSender(mCallback.getServerTitle()).unregisterServerFragmentHandler();
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

    public void sendServerMessage(final String message) {
        MessageParser.serverMessageToParse(mCallback.getServer(false), message);
    }

    private final ServerFragHandler mServerHandler = new ServerFragHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerEventType type = (ServerEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            final String message = bundle.getString(EventBundleKeys.message);
            switch (type) {
                case Error:
                    appendToTextView(message + "\n");
                    break;
                case ServerConnected:
                    mCallback.connectedToServer();
                    editText.setEnabled(true);
                    // FALL THROUGH INTENTIONAL
                case Generic:
                    appendToTextView(message + "\n");
                    break;
            }
        }
    };

    public interface ServerFragmentCallback extends CommonCallbacks {
        public void connectedToServer();
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final CharSequence text = editText.getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            editText.setText("");

            sendServerMessage(message);
        }
        return false;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.Server;
    }
}