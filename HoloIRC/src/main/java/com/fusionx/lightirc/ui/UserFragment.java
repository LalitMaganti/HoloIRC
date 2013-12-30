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
import com.fusionx.relay.Message;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.PrivateActionEvent;
import com.fusionx.relay.event.PrivateEvent;
import com.fusionx.relay.event.PrivateMessageEvent;
import com.fusionx.relay.event.PrivateNickChangeEvent;
import com.fusionx.relay.event.PrivateQuitEvent;
import com.fusionx.relay.parser.UserInputParser;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;

import java.util.List;

public class UserFragment extends IRCFragment {

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

        final Server server = mCallbacks.getServer();
        final PrivateMessageUser user = server.getPrivateMessageUserIfExists(mTitle);

        if (user.isUserQuit()) {
            onDisableUserInput();
        } else {
            server.getServerEventBus().register(this);
            user.setCached(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final Server server = mCallbacks.getServer();
        final PrivateMessageUser user = server.getPrivateMessageUserIfExists(mTitle);

        if (!user.isUserQuit()) {
            server.getServerEventBus().unregister(this);
            user.setCached(false);
        }
    }

    @Override
    protected List<Message> onRetrieveMessages() {
        final PrivateMessageUser uci = mCallbacks.getServer().getPrivateMessageUserIfExists(mTitle);
        return uci.getBuffer();
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.User;
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseUserMessage(mCallbacks.getServer(), mTitle, message);
    }

    @Subscribe
    public void onUserEvent(final PrivateMessageEvent event) {
        onProcessPrivateEvent(event);
    }

    @Subscribe
    public void onPrivateAction(final PrivateActionEvent event) {
        onProcessPrivateEvent(event);
    }

    @Subscribe
    public void onPrivateQuit(final PrivateQuitEvent event) {
        onProcessPrivateEvent(event);

        onDisableUserInput();
    }

    @Subscribe
    public void onPrivateNickChange(final PrivateNickChangeEvent event) {
        onProcessPrivateEvent(event);
    }

    private void onProcessPrivateEvent(final PrivateEvent event) {
        if (event.userNick.equals(mTitle) && StringUtils.isNotBlank(event.message)) {
            if (mMessageAdapter == null) {
                setupListAdapter();
            }
            synchronized (mMessageAdapter.getMessages()) {
                mMessageAdapter.add(new Message(event.message));
            }
        }
    }

    public interface Callbacks {

        public Server getServer();
    }
}