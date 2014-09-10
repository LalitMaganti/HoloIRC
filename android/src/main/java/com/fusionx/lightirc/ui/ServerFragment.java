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

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.misc.FragmentType;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import co.fusionx.relay.conversation.Server;
import co.fusionx.relay.event.server.ServerEvent;
import co.fusionx.relay.parser.UserInputParser;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;

public class ServerFragment extends ConversationFragment<ServerEvent> {

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
    public FragmentType getType() {
        return FragmentType.SERVER;
    }

    // Subscription methods
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final ServerEvent event) {
        final int position = mLayoutManager.findLastCompletelyVisibleItemPosition();
        mMessageAdapter.add(event);
        if (position == mMessageAdapter.getItemCount() - 2) {
            mRecyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected List<? extends ServerEvent> getAdapterData() {
        return getServer().getBuffer();
    }

    public Server getServer() {
        return (Server) mConversation;
    }
}