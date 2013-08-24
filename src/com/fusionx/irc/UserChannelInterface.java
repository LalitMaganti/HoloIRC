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

package com.fusionx.irc;

import android.content.Context;

import com.fusionx.common.Utils;
import com.fusionx.irc.enums.UserLevel;
import com.fusionx.irc.misc.IRCUserComparator;
import com.fusionx.lightlibrary.collections.TwoWayHashSet;

import java.io.OutputStreamWriter;
import java.util.Set;

import de.scrum_master.util.UpdateableTreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter(AccessLevel.PACKAGE)
public final class UserChannelInterface extends TwoWayHashSet<ChannelUser, Channel> {
    private final OutputStreamWriter outputStream;
    private final Context context;
    private final Server server;

    public UserChannelInterface(@NonNull final OutputStreamWriter outputStream,
                                Context context, final Server server) {
        this.outputStream = outputStream;
        this.context = context;
        this.server = server;
    }

    public synchronized void coupleUserAndChannel(@NonNull final ChannelUser user,
                                                  @NonNull final Channel channel) {
        user.getUserLevelMap().put(channel, UserLevel.NONE);
        addChannelToUser(user, channel);
        addUserToChannel(user, channel);
    }

    public synchronized void addChannelToUser(@NonNull final ChannelUser user,
                                              @NonNull final Channel channel) {
        super.addBToA(user, channel);
    }

    private synchronized void addUserToChannel(@NonNull final ChannelUser user,
                                               @NonNull final Channel channel) {
        UpdateableTreeSet<ChannelUser> listofUsers = bToAMap.get(channel);
        if (listofUsers == null) {
            listofUsers = new UpdateableTreeSet<>(new IRCUserComparator(channel));
            bToAMap.put(channel, listofUsers);
        }
        listofUsers.add(user);
    }

    public synchronized void decoupleUserAndChannel(@NonNull final ChannelUser user,
                                                    @NonNull final Channel channel) {
        super.decouple(user, channel);

        user.getUserLevelMap().remove(channel);
    }

    public synchronized Set<Channel> removeUser(@NonNull final ChannelUser user) {
        return super.removeObjectA(user);
    }

    public synchronized void removeChannel(@NonNull final Channel channel) {
        for (final ChannelUser user : bToAMap.remove(channel)) {
            aToBMap.get(user).remove(channel);

            user.getUserLevelMap().remove(channel);
        }
    }

    synchronized UpdateableTreeSet<ChannelUser> getAllUsersInChannel(@NonNull final Channel channel) {
        return super.getAllAInB(channel);
    }

    synchronized UpdateableTreeSet<Channel> getAllChannelsInUser(@NonNull final ChannelUser user) {
        return super.getAllBInA(user);
    }

    public synchronized ChannelUser getUserFromRaw(@NonNull final String rawSource) {
        final String nick = Utils.getNickFromRaw(rawSource);
        return getUser(nick);
    }

    public synchronized ChannelUser getUserIfExists(@NonNull final String nick) {
        for (final ChannelUser user : aToBMap.keySet()) {
            if (user.getNick().equals(nick)) {
                return user;
            }
        }
        return null;
    }

    public synchronized ChannelUser getUser(@NonNull final String nick) {
        return getUserIfExists(nick) != null ? getUserIfExists(nick) : new ChannelUser(nick, this);
    }

    public synchronized Channel getChannel(@NonNull final String name) {
        for (final Channel channel : bToAMap.keySet()) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return new Channel(name, this);
    }

    synchronized void putAppUser(@NonNull final AppUser user) {
        aToBMap.put(user, new UpdateableTreeSet<Channel>());
    }
}