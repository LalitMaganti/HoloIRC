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
import com.fusionx.lightirc.loaders.ChannelLoader;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.WorldUser;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.parser.UserInputParser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;

import java.util.List;

public final class ChannelFragment extends IRCFragment<ChannelEvent> {

    private Callbacks mCallback;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, Callbacks.class);
        }
    }

    public void onUserMention(final List<WorldUser> users) {
        final String text = String.valueOf(mMessageBox.getText());
        String nicks = "";
        for (final WorldUser userNick : users) {
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
        UserInputParser.onParseChannelMessage(mCallback.getServer(), mTitle, message);
    }

    private Channel getChannel() {
        return mCallback.getServer().getUserChannelInterface().getChannelIfExists(mTitle);
    }

    @Override
    public Loader<List<ChannelEvent>> onCreateLoader(final int id, final Bundle args) {
        return new ChannelLoader(getActivity(), mCallback.getServer(), getChannel());
    }


    // Callback interface
    public interface Callbacks {

        public Server getServer();
    }
}