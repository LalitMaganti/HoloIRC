package com.fusionx.lightirc.util;

import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import java.util.Arrays;
import java.util.List;

class BuilderUtils {

    public static List<ServerConfiguration.Builder> getFirstTimeBuilderList() {
        final ServerConfiguration.Builder freenode = new ServerConfiguration.Builder();
        freenode.setTitle("Freenode").setUrl("chat.freenode.net").setPort(6667).setSsl(false);
        freenode.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        freenode.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ServerConfiguration.Builder snoonet = new ServerConfiguration.Builder();
        snoonet.setTitle("Snoonet").setUrl("irc.snoonet.org").setPort(6667).setSsl(false);
        snoonet.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        snoonet.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");
        
        final ServerConfiguration.Builder tmwirc = new ServerConfiguration.Builder();
        tmwirc.setTitle("Techman's World IRC").setUrl("irc.techmansworld.com").setPort(6667).setSsl(false);
        tmwirc.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        tmwirc.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        return Arrays.asList(freenode, snoonet, tmwirc);
    }
}
