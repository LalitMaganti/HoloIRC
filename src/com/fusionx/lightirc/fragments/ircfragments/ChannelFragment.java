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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;

import com.fusionx.common.Utils;
import com.fusionx.irc.Channel;
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.Server;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.misc.FragmentUtils;
import com.fusionx.uiircinterface.MessageParser;

import java.util.ArrayList;

public class ChannelFragment extends IRCFragment {
    private final Handler channelFragmentHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ChannelEventType type = (ChannelEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            final ChannelFragmentCallback callback = FragmentUtils.getParent(ChannelFragment.this,
                    ChannelFragmentCallback.class);
            switch (type) {
                case UserListChanged:
                    callback.updateUserList(title);
                    if (!Utils.isMessagesFromChannelShown(getActivity())) {
                        break;
                    }
                case Generic:
                    appendToTextView(bundle.getString(EventBundleKeys.message) + "\n");
                    break;
                case UserParted:
                    callback.switchFragmentAndRemove(title);
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        final ChannelFragmentCallback callback = FragmentUtils.getParent(this,
                ChannelFragmentCallback.class);
        final Server server = callback.getServer(true);
        if (server != null) {
            final Channel channel = server.getUserChannelInterface().getChannel(title);
            if (channel != null) {
                writeToTextView(channel.getBuffer());
            }
        }
    }

    public void onUserMention(final ArrayList<ChannelUser> users) {
        final String text = String.valueOf(mEditText.getText());
        String nicks = "";
        for (final ChannelUser userNick : users) {
            nicks += Html.fromHtml(userNick.getPrettyNick(title)) + ": ";
        }

        mEditText.clearComposingText();
        mEditText.setText(nicks + text);
        mEditText.requestFocus();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.Channel;
    }

    @Override
    public Handler getHandler() {
        return channelFragmentHandler;
    }

    @Override
    public void sendMessage(final String message) {
        ChannelFragmentCallback callback = FragmentUtils.getParent(this,
                ChannelFragmentCallback.class);
        MessageParser.channelMessageToParse(getActivity(), callback.getServer(false), title,
                message);
    }

    public interface ChannelFragmentCallback {
        public void updateUserList(final String channelName);

        public Server getServer(final boolean nullAllowed);

        public void switchFragmentAndRemove(final String channelName);
    }
}