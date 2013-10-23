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

import com.fusionx.lightirc.adapters.IRCAnimationAdapter;
import com.fusionx.lightirc.communication.MessageParser;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.util.FragmentUtils;

public class UserFragment extends IRCFragment {
    @Override
    public void onResume() {
        super.onResume();

        if (getListAdapter() == null) {
            final UserFragmentCallbacks callback = FragmentUtils.getParent(this,
                    UserFragmentCallbacks.class);
            final Server server = callback.getServer();
            final PrivateMessageUser user = server.getPrivateMessageUser(title);
            final IRCAnimationAdapter adapter = new IRCAnimationAdapter(user.getBuffer
                    ());
            adapter.setAbsListView(getListView());
            user.getBuffer().setActivityContext(getActivity());
            setListAdapter(adapter);
            getListView().setSelection(getListView().getCount());
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
        MessageParser.userMessageToParse(getActivity(), callback.getServer(), title,
                message);
    }

    public interface UserFragmentCallbacks {
        public Server getServer();
    }
}