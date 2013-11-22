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

import android.content.Context;

import com.fusionx.lightirc.collections.TwoWayHashSet;
import com.fusionx.lightirc.collections.UpdateableTreeSet;
import com.fusionx.lightirc.collections.UserListTreeSet;
import com.fusionx.lightirc.irc.misc.IRCUserComparator;
import com.fusionx.lightirc.util.IRCUtils;

import java.io.OutputStreamWriter;
import java.util.Set;

import lombok.NonNull;

public final class UserChannelInterface extends TwoWayHashSet<ChannelUser, Channel> {
    private final OutputStreamWriter mOutputStream;
    private final Context mContext;
    private final Server mServer;

    public UserChannelInterface(final OutputStreamWriter outputStream,
                                final Context context, final Server server) {
        mOutputStream = outputStream;
        mContext = context;
        mServer = server;
    }

    public synchronized void coupleUserAndChannel(final ChannelUser user,
                                                  final Channel channel) {
        user.onJoin(channel);
        addChannelToUser(user, channel);
        addUserToChannel(user, channel);
    }

    public synchronized void addChannelToUser(final ChannelUser user,
                                              final Channel channel) {
        super.addBToA(user, channel);
    }

    private synchronized void addUserToChannel(final ChannelUser user,
                                               final Channel channel) {
        UserListTreeSet setOfUsers = (UserListTreeSet) bToAMap.get(channel);
        if (setOfUsers == null) {
            setOfUsers = new UserListTreeSet(new IRCUserComparator(channel));
            bToAMap.put(channel, setOfUsers);
        }
        synchronized (setOfUsers.getLock()) {
            setOfUsers.add(user);
        }
    }

    public synchronized void decoupleUserAndChannel(@NonNull final ChannelUser user,
                                                    @NonNull final Channel channel) {
        user.onRemove(channel);

        final Set<Channel> setOfChannels = aToBMap.get(user);
        if (setOfChannels != null) {
            setOfChannels.remove(channel);
            if (setOfChannels.isEmpty()) {
                aToBMap.remove(user);
            }
        }
        final UserListTreeSet setOfUsers = (UserListTreeSet) bToAMap.get(channel);
        if (setOfUsers != null) {
            synchronized (setOfUsers.getLock()) {
                setOfUsers.remove(user);
            }
            if (setOfUsers.isEmpty()) {
                bToAMap.remove(channel);
            }
        }
    }

    public synchronized Set<Channel> removeUser(@NonNull final ChannelUser user) {
        final Set<Channel> removedSet = aToBMap.remove(user);
        if (removedSet != null) {
            for (final Channel channel : removedSet) {
                final UserListTreeSet set = (UserListTreeSet) bToAMap.get(channel);
                synchronized (set.getLock()) {
                    set.remove(user);
                }
            }
        }
        return removedSet;
    }

    public synchronized void removeChannel(@NonNull final Channel channel) {
        for (final ChannelUser user : bToAMap.remove(channel)) {
            final UpdateableTreeSet<Channel> channelMap = aToBMap.get(user);
            if (channelMap != null) {
                channelMap.remove(channel);
                user.onRemove(channel);
                if (channelMap.isEmpty()) {
                    aToBMap.remove(user);
                }
            }
        }
    }

    synchronized UserListTreeSet getAllUsersInChannel(@NonNull final Channel channel) {
        return (UserListTreeSet) super.getAllAInB(channel);
    }

    synchronized UpdateableTreeSet<Channel> getAllChannelsInUser(@NonNull final ChannelUser user) {
        return super.getAllBInA(user);
    }

    public synchronized ChannelUser getUserFromRaw(@NonNull final String rawSource) {
        final String nick = IRCUtils.getNickFromRaw(rawSource);
        return getUser(nick);
    }

    public synchronized ChannelUser getUserIfExists(@NonNull final String nick) {
        for (final ChannelUser user : aToBMap.keySet()) {
            if (nick.equals(user.getNick())) {
                return user;
            }
        }
        return null;
    }

    public synchronized ChannelUser getUser(@NonNull final String nick) {
        return getUserIfExists(nick) != null ? getUserIfExists(nick) : new ChannelUser(nick, this);
    }

    public synchronized Channel getChannel(@NonNull final String name) {
        return getChannelIfExists(name) != null ? getChannelIfExists(name) : new Channel(name,
                this);
    }

    public synchronized Channel getChannelIfExists(@NonNull final String name) {
        for (final Channel channel : bToAMap.keySet()) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }

    synchronized void putAppUser(@NonNull final AppUser user) {
        aToBMap.put(user, new UpdateableTreeSet<Channel>());
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