package com.fusionx.ircinterface;

import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;

public class AppUser extends User {
    @Getter
    private HashSet<User> privateMessages = new HashSet<>();

    public AppUser(@NonNull final String nick,
                   @NonNull final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
    }

    public void newPrivateMessage(User user) {
        privateMessages.add(user);
    }

    public void closePrivateMessage(User user) {
        privateMessages.remove(user);
    }
}
