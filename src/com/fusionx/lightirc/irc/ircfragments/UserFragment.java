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

package com.fusionx.lightirc.irc.ircfragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.constants.EventBundleKeys;
import com.fusionx.lightirc.irc.enums.UserEventType;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.uiircinterface.MessageParser;
import com.fusionx.lightirc.util.FragmentUtils;

public class UserFragment extends IRCFragment {
    private final Handler userFragmentHandler = new Handler() {
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

    @Override
    public void onResume() {
        super.onResume();

        final UserFragmentCallbacks callback = FragmentUtils.getParent(this,
                UserFragmentCallbacks.class);
        final Server server = callback.getServer(true);
        if (server != null) {
            final PrivateMessageUser user = server.getPrivateMessageUser(title);
            if (user != null) {
                writeToTextView(user.getBuffer());
            }
        }
    }

    @Override
    public FragmentType getType() {
        return FragmentType.User;
    }

    @Override
    public Handler getHandler() {
        return userFragmentHandler;
    }

    @Override
    public void sendMessage(final String message) {
        UserFragmentCallbacks callback = FragmentUtils.getParent(this,
                UserFragmentCallbacks.class);
        MessageParser.userMessageToParse(getActivity(), callback.getServer(false), title,
                message);
    }

    public interface UserFragmentCallbacks {
        public Server getServer(boolean nullable);
    }
}