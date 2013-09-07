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

import android.app.Activity;
import android.text.Html;

import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.event.UserListReceivedEvent;
import com.fusionx.lightirc.uiircinterface.MessageParser;
import com.fusionx.lightirc.uiircinterface.MessageSender;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.lightirc.util.MiscUtils;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class ChannelFragment extends IRCFragment {
    private boolean mUserListMessagesShown;
    private ChannelFragmentCallback callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mUserListMessagesShown = MiscUtils.isMessagesFromChannelShown(getActivity());

        if (callback == null) {
            callback = FragmentUtils.getParent(ChannelFragment.this,
                    ChannelFragmentCallback.class);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final MessageSender sender = MessageSender.getSender(callback.getServerTitle(), true);
        if (sender != null) {
            sender.getBus().unregister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getListAdapter() == null) {
            final Server server = callback.getServer(true);
            final Channel channel = server.getUserChannelInterface().getChannel(title);
            final AlphaInAnimationAdapter adapter = new AlphaInAnimationAdapter(new
                    IRCMessageAdapter(getActivity(), channel.getBuffer()));

            adapter.setAbsListView(getListView());
            setListAdapter(adapter);
        } else {
            getListAdapter().notifyDataSetChanged();
        }

        MessageSender.getSender(callback.getServerTitle()).getBus().register(this);
    }

    public void onUserMention(final ArrayList<ChannelUser> users) {
        final String text = String.valueOf(mEditText.getText());
        String nicks = "";
        for (final ChannelUser userNick : users) {
            nicks += Html.fromHtml(userNick.getPrettyNick(title)) + ": ";
        }
        mEditText.clearComposingText();
        mEditText.append(nicks + text);
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Channel;
    }

    @Subscribe
    public void onChannelMessage(final ChannelEvent event) {
        if (title.equals(event.channelName)) {
            if (event.userListChanged) {
                callback.updateUserList(title);
            }
            if ((!event.userListChanged || mUserListMessagesShown)) {
                getListAdapter().notifyDataSetChanged();
            }
        }
    }

    @Subscribe
    public void onUserListReceived(final UserListReceivedEvent event) {
        if (event.channelName.equals(title)) {
            callback.updateUserList(title);
        }
    }

    @Override
    public void sendMessage(final String message) {
        MessageParser.channelMessageToParse(getActivity(), callback.getServer(false), title,
                message);
    }

    public interface ChannelFragmentCallback {
        public void updateUserList(final String channelName);

        public Server getServer(final boolean nullAllowed);

        public void switchFragmentAndRemove(final String channelName);

        public String getServerTitle();
    }
}