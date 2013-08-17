package com.fusionx.irc.writers;

import com.fusionx.irc.Channel;
import com.fusionx.irc.constants.WriterCommands;

import lombok.NonNull;

import java.io.OutputStreamWriter;

public class ChannelWriter extends RawWriter {
    private Channel mChannel;

    public ChannelWriter(OutputStreamWriter out, Channel channel) {
        super(out);
        mChannel = channel;
    }

    public void sendMessage(final String message) {
        writeLineToServer(String.format(WriterCommands.PRIVMSG, mChannel.getName(), message));
    }

    public void sendAction(final String action) {
        final String s = String.format(WriterCommands.ACTION, mChannel.getName(), action);
        writeLineToServer(s);
    }

    public void partChannel(@NonNull final String reason) {
        writeLineToServer(String.format(WriterCommands.PART, mChannel.getName(), reason).trim());
    }

    public void sendWho() {
        writeLineToServer(String.format(WriterCommands.WHO, mChannel.getName()));
    }
}
