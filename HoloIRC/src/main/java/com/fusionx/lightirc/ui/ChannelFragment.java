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
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ChannelUser;
import com.fusionx.relay.Message;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.ChannelEvent;
import com.fusionx.relay.parser.UserInputParser;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;

import java.util.List;

public final class ChannelFragment extends IRCFragment {

    private ChannelFragmentCallback mCallback;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, ChannelFragmentCallback.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mCallback.getServer().getServerEventBus().register(this);
        getChannel().setCached(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCallback.getServer().getServerEventBus().unregister(this);
        getChannel().setCached(false);
    }

    @Override
    protected List<Message> onRetrieveMessages() {
        return getChannel().getBuffer();
    }

    public void onUserMention(final List<ChannelUser> users) {
        final String text = String.valueOf(mMessageBox.getText());
        String nicks = "";
        for (final ChannelUser userNick : users) {
            nicks += userNick.getNick() + ": ";
        }
        mMessageBox.clearComposingText();
        mMessageBox.append(nicks + text);
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Channel;
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.channelMessageToParse(mCallback.getServer(), mTitle, message);
    }

    // Subscription methods
    @Subscribe
    public void onChannelMessage(final ChannelEvent event) {
        if (mTitle.equals(event.channelName) && !(event.userListChanged && AppPreferences
                .hideUserMessages) && StringUtils.isNotEmpty(event.message)) {
            if (mMessageAdapter == null) {
                setupListAdapter();
            }
            synchronized (mMessageAdapter.getMessages()) {
                mMessageAdapter.add(new Message(event.message));
            }
        }
    }

    private Channel getChannel() {
        return mCallback.getServer().getUserChannelInterface().getChannel(mTitle);
    }

    // Callback interface
    public interface ChannelFragmentCallback {

        public Server getServer();
    }
}