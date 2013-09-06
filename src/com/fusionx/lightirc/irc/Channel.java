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

package com.fusionx.lightirc.irc;

import android.text.Html;
import android.text.Spanned;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.writers.ChannelWriter;
import com.fusionx.lightirc.util.MiscUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import de.scrum_master.util.UpdateableTreeSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class Channel implements Comparable<Channel>, UpdateableTreeSet.Updateable {
    @Getter
    protected final String name;
    @Getter
    protected final ArrayList<Spanned> buffer = new ArrayList<>();

    @Getter
    protected final ChannelWriter writer;
    protected final UserChannelInterface mUserChannelInterface;

    @Getter
    @Setter
    protected String topic;

    private boolean mUserListMessagesShown;

    protected Channel(@NonNull final String channelName,
                      @NonNull final UserChannelInterface userChannelInterface) {
        name = channelName;
        writer = new ChannelWriter(userChannelInterface.getOutputStream(), this);
        mUserChannelInterface = userChannelInterface;

        final String message = String.format(userChannelInterface.getContext().getString(R.string
                .parser_joined_channel), userChannelInterface
                .getServer().getUser().getColorfulNick());
        buffer.add(Html.fromHtml(message));

        mUserListMessagesShown = MiscUtils.isMessagesFromChannelShown(mUserChannelInterface
                .getContext());
    }

    @Override
    public int compareTo(final Channel channel) {
        return name.compareTo(channel.name);
    }

    public UpdateableTreeSet<ChannelUser> getUsers() {
        return mUserChannelInterface.getAllUsersInChannel(this);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Channel && ((Channel) o).name.equals(name);
    }

    @Override
    public void update() {
        throw new IllegalArgumentException();
    }

    @Override
    public void update(final Object newValue) {
        throw new IllegalArgumentException();
    }

    public void onChannelEvent(final ChannelEvent event) {
        if((!event.userListChanged || mUserListMessagesShown) && StringUtils.isNotEmpty(event
                .message)) {
            buffer.add(Html.fromHtml(event.message));
        }
    }
}