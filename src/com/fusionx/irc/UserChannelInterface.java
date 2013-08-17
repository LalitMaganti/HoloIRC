package com.fusionx.irc;

import android.content.Context;

import com.fusionx.irc.enums.UserLevel;
import com.fusionx.irc.misc.IRCUserComparator;
import com.fusionx.irc.misc.Utils;
import com.fusionx.lightlibrary.collections.TwoWayHashSet;

import de.scrum_master.util.UpdateableTreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.io.OutputStreamWriter;
import java.util.Set;

@Getter(AccessLevel.PACKAGE)
public final class UserChannelInterface extends TwoWayHashSet<User, Channel> {
    private final OutputStreamWriter outputStream;
    private final Context context;
    private final Server server;

    public UserChannelInterface(@NonNull final OutputStreamWriter outputStream,
                                Context context, final Server server) {
        this.outputStream = outputStream;
        this.context = context;
        this.server = server;
    }

    public synchronized void coupleUserAndChannel(@NonNull final User user,
                                                  @NonNull final Channel channel) {
        user.getUserLevelMap().put(channel, UserLevel.NONE);
        addChannelToUser(user, channel);
        addUserToChannel(user, channel);
    }

    public synchronized void addChannelToUser(@NonNull final User user,
                                              @NonNull final Channel channel) {
        addBToA(user, channel);
    }

    private synchronized void addUserToChannel(@NonNull final User user,
                                               @NonNull final Channel channel) {
        UpdateableTreeSet<User> listofUsers = bToAMap.get(channel);
        if (listofUsers == null) {
            listofUsers = new UpdateableTreeSet<>(new IRCUserComparator(channel));
            bToAMap.put(channel, listofUsers);
        }
        listofUsers.add(user);
    }

    public synchronized void decoupleUserAndChannel(@NonNull final User user,
                                                    @NonNull final Channel channel) {
        super.decouple(user, channel);

        user.getUserLevelMap().remove(channel);
    }

    public synchronized Set<Channel> removeUser(@NonNull final User user) {
        return super.removeObjectA(user);
    }

    public synchronized void removeChannel(@NonNull final Channel channel) {
        for (final User user : bToAMap.remove(channel)) {
            aToBMap.get(user).remove(channel);

            user.getUserLevelMap().remove(channel);
        }
    }

    synchronized UpdateableTreeSet<User> getAllUsersInChannel(@NonNull final Channel user) {
        return super.getAllAInB(user);
    }

    synchronized UpdateableTreeSet<Channel> getAllChannelsInUser(@NonNull final User user) {
        return super.getAllBInA(user);
    }

    public synchronized User getUserFromRaw(@NonNull final String rawSource) {
        final String nick = Utils.getNickFromRaw(rawSource);
        return getUser(nick);
    }

    public synchronized User getUserIfExists(@NonNull final String nick) {
        for (final User user : aToBMap.keySet()) {
            if (user.getNick().equals(nick)) {
                return user;
            }
        }
        return null;
    }

    public synchronized User getUser(@NonNull final String nick) {
        return getUserIfExists(nick) != null ? getUserIfExists(nick)
                : new User(nick, this);
    }

    public synchronized Channel getChannel(@NonNull final String name) {
        for (final Channel channel : bToAMap.keySet()) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return new Channel(name, this);
    }

    synchronized void putAppUser(@NonNull final User user) {
        aToBMap.put(user, new UpdateableTreeSet<Channel>());
    }
}