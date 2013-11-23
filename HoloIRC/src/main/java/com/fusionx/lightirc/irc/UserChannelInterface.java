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
import com.fusionx.lightirc.irc.misc.IRCUserComparator;
import com.fusionx.lightirc.util.IRCUtils;

import android.content.Context;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;

public final class UserChannelInterface {

    protected final HashMap<ChannelUser, UpdateableTreeSet<Channel>> mUserToChannelMap;

    protected final HashMap<Channel, UpdateableTreeSet<ChannelUser>> mChannelToUserMap;

    private final OutputStreamWriter mOutputStream;

    private final Context mContext;

    private final Server mServer;


    public UserChannelInterface(final OutputStreamWriter outputStream,
            final Context context, final Server server) {
        mOutputStream = outputStream;
        mContext = context;
        mServer = server;
        mUserToChannelMap = new HashMap<ChannelUser, UpdateableTreeSet<Channel>>();
        mChannelToUserMap = new HashMap<Channel, UpdateableTreeSet<ChannelUser>>();
    }

    public synchronized void coupleUserAndChannel(final ChannelUser user,
            final Channel channel) {
        user.onJoin(channel);
        addChannelToUser(user, channel);
        addUserToChannel(user, channel);
    }

    public synchronized void addChannelToUser(final ChannelUser user,
            final Channel channel) {
        UpdateableTreeSet<Channel> list = mUserToChannelMap.get(user);
        if (list == null) {
            list = new UpdateableTreeSet<Channel>();
            mUserToChannelMap.put(user, list);
        }
        list.add(channel);
    }

    private synchronized void addUserToChannel(final ChannelUser user,
            final Channel channel) {
        UserListTreeSet setOfUsers = (UserListTreeSet) mChannelToUserMap.get(channel);
        if (setOfUsers == null) {
            setOfUsers = new UserListTreeSet(new IRCUserComparator(channel));
            mChannelToUserMap.put(channel, setOfUsers);
        }
        synchronized (setOfUsers.getLock()) {
            setOfUsers.add(user);
        }
    }

    public synchronized void decoupleUserAndChannel(final ChannelUser user, final Channel channel) {
        user.onRemove(channel);

        final Set<Channel> setOfChannels = mUserToChannelMap.get(user);
        if (setOfChannels != null) {
            setOfChannels.remove(channel);
            if (setOfChannels.isEmpty()) {
                mUserToChannelMap.remove(user);
            }
        }
        final UserListTreeSet setOfUsers = (UserListTreeSet) mChannelToUserMap.get(channel);
        if (setOfUsers != null) {
            synchronized (setOfUsers.getLock()) {
                setOfUsers.remove(user);
            }
            if (setOfUsers.isEmpty()) {
                mChannelToUserMap.remove(channel);
            }
        }
    }

    public synchronized Set<Channel> removeUser(final ChannelUser user) {
        final Set<Channel> removedSet = mUserToChannelMap.remove(user);
        if (removedSet != null) {
            for (final Channel channel : removedSet) {
                final UserListTreeSet set = (UserListTreeSet) mChannelToUserMap.get(channel);
                synchronized (set.getLock()) {
                    set.remove(user);
                }
            }
        }
        return removedSet;
    }

    public synchronized void removeChannel(final Channel channel) {
        for (final ChannelUser user : mChannelToUserMap.remove(channel)) {
            final UpdateableTreeSet<Channel> channelMap = mUserToChannelMap.get(user);
            if (channelMap != null) {
                channelMap.remove(channel);
                user.onRemove(channel);
                if (channelMap.isEmpty()) {
                    mUserToChannelMap.remove(user);
                }
            }
        }
    }

    synchronized UserListTreeSet getAllUsersInChannel(final Channel channel) {
        return (UserListTreeSet) mChannelToUserMap.get(channel);
    }

    synchronized UpdateableTreeSet<Channel> getAllChannelsInUser(final ChannelUser user) {
        return mUserToChannelMap.get(user);
    }

    public synchronized ChannelUser getUserFromRaw(final String rawSource) {
        final String nick = IRCUtils.getNickFromRaw(rawSource);
        return getUser(nick);
    }

    public synchronized ChannelUser getUserIfExists(final String nick) {
        for (final ChannelUser user : mUserToChannelMap.keySet()) {
            if (nick.equals(user.getNick())) {
                return user;
            }
        }
        return null;
    }

    public synchronized ChannelUser getUser(final String nick) {
        return getUserIfExists(nick) != null ? getUserIfExists(nick) : new ChannelUser(nick, this);
    }

    public synchronized Channel getChannel(final String name) {
        return getChannelIfExists(name) != null ? getChannelIfExists(name) : new Channel(name,
                this);
    }

    public synchronized Channel getChannelIfExists(final String name) {
        for (final Channel channel : mChannelToUserMap.keySet()) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }

    synchronized void putAppUser(final AppUser user) {
        mUserToChannelMap.put(user, new UpdateableTreeSet<Channel>());
    }

    // Getters and setters
    OutputStreamWriter getOutputStream() {
        return mOutputStream;
    }

    Context getContext() {
        return mContext;
    }

    Server getServer() {
        return mServer;
    }
}