package com.fusionx.ircinterface.writers;

import com.fusionx.ircinterface.User;
import com.fusionx.ircinterface.constants.WriterCommands;

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
