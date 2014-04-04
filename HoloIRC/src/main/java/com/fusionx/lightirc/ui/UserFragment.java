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

import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.event.user.UserEvent;
import com.fusionx.relay.interfaces.Conversation;
import com.fusionx.relay.parser.UserInputParser;
import com.squareup.otto.Subscribe;

import java.util.List;

public class UserFragment extends IRCFragment<UserEvent> {

    @Override
    public void onResume() {
        super.onResume();

        final PrivateMessageUser user = getPrivateMessageUser();

        if (user.isUserQuit()) {
            // TODO - fix this
            //onResetUserInput();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.USER;
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseUserMessage(getServer(), mTitle, message);
    }

    @Override
    protected List<UserEvent> getAdapterData() {
        return getPrivateMessageUser().getBuffer();
    }

    @Override
    protected List<UserEvent> getDisconnectedAdapterData() {
        return getAdapterData();
    }

    public PrivateMessageUser getPrivateMessageUser() {
        return getServer().getUserChannelInterface().getPrivateMessageUser(mTitle);
    }

    // Subscription methods
    @Subscribe
    public void onPrivateEvent(final UserEvent event) {
        if (event.user.getNick().equals(getPrivateMessageUser().getNick())) {
            mMessageAdapter.add(event);
        }
    }
}