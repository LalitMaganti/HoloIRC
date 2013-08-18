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

import com.fusionx.irc.Channel;
import com.fusionx.irc.Server;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.lightirc.handlerabstract.PMFragmentHandler;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.uiircinterface.MessageParser;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

public class PMFragment extends IRCFragment {
    private CommonCallbacks mCallback;

    @Override
    public void onResume() {
        super.onResume();

        MessageSender.getSender(mCallback.getServerTitle())
                .registerUserFragmentHandler(getTitle(), mUserFragmentHandler);

        final Server server = mCallback.getServer(true);
        if (server != null) {
            final User user = server.getUserChannelInterface().getUser(getTitle());
            if (user != null) {
                writeToTextView(user.getBuffer());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        MessageSender.getSender(mCallback.getServerTitle())
                .unregisterUserFragmentHandler(getTitle());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (CommonCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement PMFragmentCallbacks");
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        final CharSequence text = mEditText.getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            mEditText.setText("");

            sendUserMessage(getTitle(), message);
        }
        return false;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.User;
    }

    public void sendUserMessage(final String nick, final String message) {
        MessageParser.userMessageToParse(mCallback, nick, message);
    }

    private final PMFragmentHandler mUserFragmentHandler = new PMFragmentHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final UserEventType type = (UserEventType) bundle.getSerializable(EventBundleKeys.eventType);
            final String message = bundle.getString(EventBundleKeys.message);
            switch (type) {
                case Generic:
                    appendToTextView(message + "\n");
            }
        }
    };
}