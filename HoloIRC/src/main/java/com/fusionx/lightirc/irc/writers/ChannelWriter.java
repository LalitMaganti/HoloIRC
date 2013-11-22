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

package com.fusionx.lightirc.irc.writers;

import com.fusionx.lightirc.irc.Channel;

import org.apache.commons.lang3.StringUtils;

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
        final String s = String.format(WriterCommands.Action, mChannel.getName(), action);
        writeLineToServer(s);
    }

    public void partChannel(final String reason) {
        writeLineToServer(StringUtils.isEmpty(reason) ?
                String.format(WriterCommands.Part, mChannel.getName()) :
                String.format(WriterCommands.PartWithReason, mChannel.getName(), reason).trim());
    }

    public void sendWho() {
        writeLineToServer(String.format(WriterCommands.WHO, mChannel.getName()));
    }
}