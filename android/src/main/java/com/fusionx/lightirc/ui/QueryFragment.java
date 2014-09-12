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

import java.util.List;

import co.fusionx.relay.conversation.QueryUser;
import co.fusionx.relay.event.query.QueryEvent;

public class QueryFragment extends ConversationFragment<QueryEvent> {

    public QueryUser getQueryUser() {
        return (QueryUser) mConversation;
    }

    // Subscription methods
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final QueryEvent event) {
        final int position = mLayoutManager.findLastCompletelyVisibleItemPosition();
        mMessageAdapter.add(event);
        if (position == mMessageAdapter.getItemCount() - 2) {
            mRecyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onSendMessage(final String message) {
        mSession.getInputParser().parseQueryMessage(getQueryUser(), message);
    }

    @Override
    public FragmentType getType() {
        return FragmentType.USER;
    }

    @Override
    protected List<? extends QueryEvent> getAdapterData() {
        return getQueryUser().getBuffer();
    }
}