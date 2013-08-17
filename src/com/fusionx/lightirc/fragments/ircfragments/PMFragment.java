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
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fusionx.irc.Channel;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.UserEventType;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.parser.MessageParser;
import com.fusionx.uiircinterface.MessageSender;

public class PMFragment extends IRCFragment {
    private CommonCallbacks mCallback;

    @Override
    public void onResume() {
        super.onResume();

        MessageSender.getSender(mCallback.getServer(false).getTitle())
                .registerUserFragmentHandler(getTitle(), mUserFragmentHandler);

        final User user = mCallback.getServer(false).getUserChannelInterface()
                .getUser(getTitle());
        if (user != null) {
            writeToTextView(user.getBuffer());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        MessageSender.getSender(mCallback.getServer(false).getTitle())
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
        final CharSequence text = getEditText().getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && text != null
                && !text.equals("")) {
            final String message = text.toString();
            getEditText().setText("");

            sendUserMessage(getTitle(), message);
        }
        return false;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.User;
    }

    public void sendUserMessage(final String nick, final String message) {
        MessageParser.userMessageToParse(mCallback.getServer(false), nick, message);
    }

    private final Handler mUserFragmentHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final UserEventType type = (UserEventType) bundle.getSerializable(EventBundleKeys.eventType);
            switch (type) {
                case Generic:
                    appendToTextView(bundle.getString(EventBundleKeys.message) + "\n");
            }
        }
    };
}