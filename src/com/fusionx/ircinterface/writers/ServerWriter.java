package com.fusionx.ircinterface.writers;

import org.apache.commons.lang3.StringUtils;

import java.io.OutputStreamWriter;

public class ServerWriter extends RawWriter {
    public ServerWriter(OutputStreamWriter out) {
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
}