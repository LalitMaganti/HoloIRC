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
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.Message;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.UserChannelInterface;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.ColourParserUtils;
import com.fusionx.lightirc.util.FragmentUtils;
import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;

import java.util.List;

public final class ChannelFragment extends IRCFragment {

    /**
     * Callback interface for the activity or parent fragment
     */
    private ChannelFragmentCallback mCallback;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, ChannelFragmentCallback.class);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        MessageSender.getSender(mCallback.getServer().getTitle())
                .getBus().register(this);
        mCallback.getServer().getUserChannelInterface().getChannel(mTitle).setCached(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        MessageSender.getSender(mCallback.getServer().getTitle())
                .getBus().unregister(this);
        mCallback.getServer().getUserChannelInterface().getChannel(mTitle).setCached(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Message> onRetrieveMessages() {
        final UserChannelInterface uci = mCallback.getServer().getUserChannelInterface();
        return uci.getChannel(mTitle).getBuffer();
    }

    public void onUserMention(final List<ChannelUser> users) {
        final String text = String.valueOf(mMessageBox.getText());
        String nicks = "";
        for (final ChannelUser userNick : users) {
            nicks += ColourParserUtils.parseMarkup(userNick.getPrettyNick(mTitle)) + ": ";
        }
        mMessageBox.clearComposingText();
        mMessageBox.append(nicks + text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSendMessage(final String message) {
        MessageParser.channelMessageToParse(getActivity(), mCallback.getServer(), mTitle,
                message);
    }

    // Subscription methods
    @Subscribe
    public void onChannelMessage(final ChannelEvent event) {
        if ((!event.userListChanged || !AppPreferences.hideUserMessages) && StringUtils
                .isNotEmpty(event.message) && mTitle.equals(event.channelName)) {
            if(mMessageAdapter == null) {
                setupListAdapter();
            }
            synchronized (mMessageAdapter.getMessages()) {
                mMessageAdapter.add(new Message(event.message));
            }
        }
    }

    // Callback interface
    public interface ChannelFragmentCallback {

        public Server getServer();
    }
}