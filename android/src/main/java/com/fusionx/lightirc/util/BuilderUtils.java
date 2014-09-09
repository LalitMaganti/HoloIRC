package com.fusionx.lightirc.util;

import java.util.Arrays;
import java.util.List;

import co.fusionx.relay.core.ConnectionConfiguration;
import co.fusionx.relay.misc.NickStorage;

class BuilderUtils {

    public static List<ConnectionConfiguration.Builder> getFirstTimeBuilderList() {
        final ConnectionConfiguration.Builder freenode = new ConnectionConfiguration.Builder();
        freenode.setTitle("Freenode").setUrl("chat.freenode.net").setPort(6667).setSsl(false);
        freenode.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        freenode.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ConnectionConfiguration.Builder snoonet = new ConnectionConfiguration.Builder();
        snoonet.setTitle("Snoonet").setUrl("irc.snoonet.org").setPort(6667).setSsl(false);
        snoonet.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        snoonet.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ConnectionConfiguration.Builder tmwirc = new ConnectionConfiguration.Builder();
        tmwirc.setTitle("Techman's World IRC").setUrl("irc.techmansworld.com").setPort(6667)
                .setSsl(false);
        tmwirc.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        tmwirc.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        return Arrays.asList(freenode, snoonet, tmwirc);
    }
}
