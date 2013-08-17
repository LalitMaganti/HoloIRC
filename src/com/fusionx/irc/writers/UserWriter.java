package com.fusionx.irc.writers;

import com.fusionx.irc.User;
import com.fusionx.irc.constants.WriterCommands;

import java.io.OutputStreamWriter;

public class UserWriter extends RawWriter {
    private final User mUser;

    public UserWriter(OutputStreamWriter writer, final User user) {
        super(writer);
        mUser = user;
    }

    public void sendMessage(String message) {
        writeLineToServer(String.format(WriterCommands.PRIVMSG, mUser.getNick(), message));
    }

    public void sendAction(String action) {
        writeLineToServer(String.format(WriterCommands.ACTION, mUser.getNick(), action));
    }

    public void sendWho() {
        writeLineToServer(String.format(WriterCommands.WHO, mUser.getNick()));
    }
}
