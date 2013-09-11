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

import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.collections.UpdateableTreeSet;
import com.fusionx.lightirc.collections.UserListTreeSet;
import com.fusionx.lightirc.constants.UserLevelEnum;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.writers.ChannelWriter;
import com.fusionx.lightirc.util.MiscUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class Channel implements Comparable<Channel>, UpdateableTreeSet.Updateable {
    @Getter
    protected final String name;
    @Getter
    protected final ChannelWriter writer;
    @Getter
    protected IRCMessageAdapter buffer;
    @Getter
    @Setter
    protected String topic;

    private final UserChannelInterface mUserChannelInterface;
    private final Handler mAdapterHandler;
    private final HashMap<UserLevelEnum, Integer> mNumberOfUsers = new HashMap<>();
    private boolean mUserListMessagesShown;

    Channel(final String channelName, final UserChannelInterface
            userChannelInterface, final Handler adapterHandler) {
        adapterHandler.post(new Runnable() {
            @Override
            public void run() {
                buffer = new IRCMessageAdapter(userChannelInterface.getContext(),
                        new ArrayList<Spanned>());
                final String message = String.format(userChannelInterface.getContext().getString
                        (R.string.parser_joined_channel), userChannelInterface
                        .getServer().getUser().getColorfulNick());
                SpannableString string = new SpannableString(message);
                string.setSpan(message, 0, message.indexOf(" "),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                buffer.add(Html.fromHtml(message));

            }
        });
        name = channelName;
        writer = new ChannelWriter(userChannelInterface.getOutputStream(), this);
        mUserChannelInterface = userChannelInterface;
        mAdapterHandler = adapterHandler;

        // Number of users
        mNumberOfUsers.put(UserLevelEnum.OP, 0);
        mNumberOfUsers.put(UserLevelEnum.VOICE, 0);

        mUserListMessagesShown = MiscUtils.isMessagesFromChannelShown(mUserChannelInterface
                .getContext());
    }

    @Override
    public int compareTo(final Channel channel) {
        return name.compareTo(channel.name);
    }

    public UserListTreeSet getUsers() {
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
        if ((!event.userListChanged || mUserListMessagesShown) && StringUtils.isNotEmpty(event
                .message)) {
            mAdapterHandler.post(new Runnable() {
                @Override
                public void run() {
                    buffer.add(Html.fromHtml(event.message));
                }
            });
        }
    }

    public int getNumberOfUsers() {
        return getUsers().size();
    }

    public void incrementOps() {
        synchronized (mNumberOfUsers) {
            Integer numberOfOps = mNumberOfUsers.get(UserLevelEnum.OP);
            if (numberOfOps == null) {
                numberOfOps = 1;
            } else {
                ++numberOfOps;
            }
            mNumberOfUsers.put(UserLevelEnum.OP, numberOfOps);
        }
    }

    public void decrementOps() {
        synchronized (mNumberOfUsers) {
            Integer numberOfOps = mNumberOfUsers.get(UserLevelEnum.OP);
            --numberOfOps;
            mNumberOfUsers.put(UserLevelEnum.OP, numberOfOps);
        }
    }

    public void incrementVoices() {
        synchronized (mNumberOfUsers) {
            Integer numberOfVoices = mNumberOfUsers.get(UserLevelEnum.VOICE);
            if (numberOfVoices == null) {
                numberOfVoices = 1;
            } else {
                ++numberOfVoices;
            }
            mNumberOfUsers.put(UserLevelEnum.VOICE, numberOfVoices);
        }
    }

    public void decrementVoices() {
        synchronized (mNumberOfUsers) {
            Integer numberOfVoices = mNumberOfUsers.get(UserLevelEnum.VOICE);
            --numberOfVoices;
            mNumberOfUsers.put(UserLevelEnum.VOICE, numberOfVoices);
        }
    }

    public int getNumberOfOwners() {
        synchronized (mNumberOfUsers) {
            return mNumberOfUsers.get(UserLevelEnum.OP);
        }
    }

    public int getNumberOfVoices() {
        synchronized (mNumberOfUsers) {
            return mNumberOfUsers.get(UserLevelEnum.VOICE);
        }
    }

    public int getNumberOfNormalUsers() {
        int normalUsers;
        synchronized (mNumberOfUsers) {
            normalUsers = getNumberOfUsers() - (getNumberOfOwners() + getNumberOfVoices());
        }
        return normalUsers;
    }
}