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

package com.fusionx.irc.writers;

import com.fusionx.irc.core.PrivateMessageUser;

import java.io.OutputStreamWriter;

public class UserWriter extends RawWriter {
    private final PrivateMessageUser mUser;

    public UserWriter(OutputStreamWriter writer, final PrivateMessageUser user) {
        super(writer);
        mUser = user;
    }

    public void sendMessage(String message) {
        writeLineToServer(String.format(WriterCommands.PRIVMSG, mUser.getNick(), message));
    }

    public void sendAction(String action) {
        writeLineToServer(String.format(WriterCommands.Action, mUser.getNick(), action));
    }
}