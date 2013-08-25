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

import android.util.Base64;

import org.apache.commons.lang3.StringUtils;

import java.io.OutputStreamWriter;

public class ServerWriter extends RawWriter {
    public ServerWriter(final OutputStreamWriter out) {
        super(out);
    }

    public void sendUser(String userName, String hostName, String serverName, String realName) {
        writeLineToServer("USER " + userName + " " + hostName + " " + serverName + " :" + realName);
    }

    public void changeNick(final String nick) {
        writeLineToServer("NICK " + nick);
    }

    public void joinChannel(final String channelName) {
        writeLineToServer("JOIN " + channelName);
    }

    public void quitServer(final String reason) {
        writeLineToServer(StringUtils.isEmpty(reason) ? "QUIT" : "QUIT :" + reason);
    }

    public void pongServer(final String absoluteURL) {
        writeLineToServer("PONG " + absoluteURL);
    }

    public void sendServerPassword(final String password) {
        writeLineToServer("PASS " + password);
    }

    public void sendNickServPassword(final String password) {
        writeLineToServer("NICKSERV IDENTIFY " + password);
    }

    public void sendChannelMode(final String channel, final String mode, final String userNick) {
        writeLineToServer("MODE " + channel + " " + mode + " " + userNick);
    }

    public void getSupportedCapabilities() {
        writeLineToServer("CAP LS");
    }

    public void sendEndCap() {
        writeLineToServer("CAP END");
    }

    public void requestSasl() {
        writeLineToServer("CAP REQ : sasl multi-prefix");
    }

    public void sendPlainSaslAuthentication() {
        writeLineToServer("AUTHENTICATE PLAIN");
    }

    public void sendSaslAuthentication(final String saslUsername, final String saslPassword) {
        final String authentication = saslUsername + "\0" + saslUsername + "\0" + saslPassword;
        final String encoded = Base64.encodeToString(authentication.getBytes(), Base64.DEFAULT);
        writeLineToServer("AUTHENTICATE " + encoded);
    }
}