package com.fusionx.irc;

import java.util.HashSet;

import lombok.Getter;
import lombok.NonNull;

public class AppUser extends User {
    @Getter
    private HashSet<User> privateMessages = new HashSet<>();

    public AppUser(@NonNull final String nick,
                   @NonNull final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
        userChannelInterface.putAppUser(this);
    }

    public void newPrivateMessage(User user) {
        privateMessages.add(user);
    }

    public void closePrivateMessage(User user) {
        privateMessages.remove(user);
    }
}
