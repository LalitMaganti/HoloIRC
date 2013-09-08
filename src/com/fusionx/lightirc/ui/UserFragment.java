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
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.util.FragmentUtils;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class UserFragment extends IRCFragment {
    @Override
    public void onResume() {
        super.onResume();

        if (getListAdapter() == null) {
            final UserFragmentCallbacks callback = FragmentUtils.getParent(this,
                    UserFragmentCallbacks.class);
            final Server server = callback.getServer(true);
            final PrivateMessageUser channel = server.getPrivateMessageUser(title);
            final AlphaInAnimationAdapter adapter = new AlphaInAnimationAdapter(channel.getBuffer
                    ());
            adapter.setAbsListView(getListView());
            setListAdapter(adapter);
        } else {
            getListAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.User;
    }

    @Override
    public void sendMessage(final String message) {
        UserFragmentCallbacks callback = FragmentUtils.getParent(this,
                UserFragmentCallbacks.class);
        MessageParser.userMessageToParse(getActivity(), callback.getServer(false), title,
                message);
    }

    public interface UserFragmentCallbacks {
        public Server getServer(boolean nullable);

        public String getServerTitle();
    }
}