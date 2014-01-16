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

import com.fusionx.lightirc.constants.FragmentType;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.user.UserEvent;
import com.fusionx.relay.parser.UserInputParser;
import com.squareup.otto.Subscribe;

import android.app.Activity;

import java.util.List;

public class UserFragment extends IRCFragment<UserEvent> {

    private Callbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mCallbacks == null) {
            mCallbacks = FragmentUtils.getParent(this, Callbacks.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final PrivateMessageUser user = getPrivateMessageUser();

        if (user.isUserQuit()) {
            onDisableUserInput();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.User;
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseUserMessage(mCallbacks.getServer(), mTitle, message);
    }

    public Server getServer() {
        return mCallbacks.getServer();
    }

    @Override
    protected List<UserEvent> getAdapterData() {
        return getPrivateMessageUser().getBuffer();
    }

    public PrivateMessageUser getPrivateMessageUser() {
        return getServer().getUserChannelInterface().getPrivateMessageUserIfExists(mTitle);
    }

    // Subscription methods
    @Subscribe
    public void onPrivateEvent(final UserEvent event) {
        if (event.user.getNick().equals(getPrivateMessageUser().getNick())) {
            mMessageAdapter.add(event);
        }
    }

    public interface Callbacks {

        public Server getServer();
    }
}