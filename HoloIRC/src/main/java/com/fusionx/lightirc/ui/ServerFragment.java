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

import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.event.server.DisconnectEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.ServerEvent;
import com.fusionx.relay.interfaces.Conversation;
import com.fusionx.relay.parser.UserInputParser;
import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;

public class ServerFragment extends IRCFragment<ServerEvent> {

    private static final ImmutableList<? extends Class<? extends ServerEvent>> sClasses =
            ImmutableList.of(JoinEvent.class, PartEvent.class, DisconnectEvent.class);

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseServerMessage(getServer(), message);
    }

    @Override
    protected List<ServerEvent> getAdapterData() {
        return getServer().getBuffer();
    }

    @Override
    protected List<ServerEvent> getDisconnectedAdapterData() {
        return getAdapterData();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.SERVER;
    }

    @Override
    public Conversation getConversation() {
        return getServer();
    }

    // Subscription methods
    @Subscribe
    public void onServerEvent(final ServerEvent event) {
        if (!sClasses.contains(event.getClass())) {
            mMessageAdapter.add(event);
        }
    }
}