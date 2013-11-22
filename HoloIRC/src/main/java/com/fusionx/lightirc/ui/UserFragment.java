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

import com.fusionx.lightirc.communication.MessageParser;
import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.Message;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.util.FragmentUtils;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;

import java.util.List;

public class UserFragment extends IRCFragment {

    private UserFragmentCallback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, UserFragmentCallback.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        MessageSender.getSender(mCallback.getServer().getTitle())
                .getBus().register(this);
        mCallback.getServer().getPrivateMessageUser(mTitle).setCached(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        MessageSender.getSender(mCallback.getServer().getTitle())
                .getBus().unregister(this);
        mCallback.getServer().getPrivateMessageUser(mTitle).setCached(false);
    }

    @Override
    protected List<Message> onRetrieveMessages() {
        final PrivateMessageUser uci = mCallback.getServer().getPrivateMessageUser(mTitle);
        return uci.getBuffer();
    }

    @Override
    protected void onPersistMessages(List<Message> list) {
        final PrivateMessageUser user = mCallback.getServer().getPrivateMessageUser(mTitle);
        user.setBuffer(list);
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.User;
    }

    @Override
    public void onSendMessage(final String message) {
        MessageParser.userMessageToParse(getActivity(), mCallback.getServer(), mTitle,
                message);
    }

    @Subscribe
    public void onUserEvent(final UserEvent event) {
        if (StringUtils.isNotBlank(event.message)) {
            mMessageAdapter.add(new Message(event.message));
        }
    }

    public interface UserFragmentCallback {

        public Server getServer();
    }
}