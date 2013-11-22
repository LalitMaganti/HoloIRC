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

import com.fusionx.lightirc.collections.UpdateableTreeSet;
import com.fusionx.lightirc.collections.UserListTreeSet;
import com.fusionx.lightirc.constants.UserLevelEnum;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.writers.ChannelWriter;
import com.fusionx.lightirc.misc.AppPreferences;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

public class Channel implements Comparable<Channel>, UpdateableTreeSet.Updateable {

    protected final String mName;

    protected String mTopic;

    private final UserChannelInterface mUserChannelInterface;

    private List<Message> mBuffer;

    private boolean mCached;

    protected final ChannelWriter mWriter;

    private final HashMap<UserLevelEnum, Integer> mNumberOfUsers = new HashMap<UserLevelEnum,
            Integer>();

    Channel(final String channelName, final UserChannelInterface
            userChannelInterface) {
        mName = channelName;
        mWriter = new ChannelWriter(userChannelInterface.getOutputStream(), this);
        mUserChannelInterface = userChannelInterface;

        // Number of users
        mNumberOfUsers.put(UserLevelEnum.OP, 0);
        mNumberOfUsers.put(UserLevelEnum.VOICE, 0);
    }

    @Override
    public int compareTo(final Channel channel) {
        return mName.compareTo(channel.mName);
    }

    public UserListTreeSet getUsers() {
        return mUserChannelInterface.getAllUsersInChannel(this);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Channel && ((Channel) o).mName.equals(mName);
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
        if ((!event.userListChanged || !AppPreferences.hideUserMessages) && StringUtils
                .isNotEmpty(event.message) && mBuffer != null) {
            mBuffer.add(new Message(event.message));
        }
    }

    public int getNumberOfUsers() {
        if (getUsers() != null) {
            return getUsers().size();
        } else {
            return 0;
        }
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

    // Getters and setters
    public String getName() {
        return mName;
    }

    public ChannelWriter getWriter() {
        return mWriter;
    }

    public List<Message> getBuffer() {
        return mBuffer;
    }

    public void setBuffer(List<Message> buffer) {
        mBuffer = buffer;
    }

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String mTopic) {
        this.mTopic = mTopic;
    }

    public boolean isCached() {
        return mCached;
    }

    public void setCached(boolean cached) {
        mCached = cached;
    }
}