package com.fusionx.lightirc.util;

import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.misc.NickStorage;

import java.util.Arrays;
import java.util.List;

public class BuilderUtils {

    public static List<ServerConfiguration.Builder> getFirstTimeBuilderList() {
        final ServerConfiguration.Builder freenode = new ServerConfiguration.Builder();
        freenode.setTitle("Freenode").setUrl("chat.freenode.net").setPort(6697).setSsl(true);
        freenode.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        freenode.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ServerConfiguration.Builder snoonet = new ServerConfiguration.Builder();
        snoonet.setTitle("Snoonet").setUrl("irc.snoonet.org").setPort(6697).setSsl(true);
        snoonet.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        snoonet.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");
        
        final ServerConfiguration.Builder tmwirc = new ServerConfiguration.Builder();
        tmwirc.setTitle("Techman's World IRC").setUrl("irc.techmansworld.com").setPort(6667).setSsl(false);
        tmwirc.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        tmwirc.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ServerConfiguration.Builder snyde = new ServerConfiguration.Builder();
        snyde.setTitle("Snyde").setUrl("irc.snyde.net").setPort(6667).setSsl(false);
        snyde.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        snyde.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        return Arrays.asList(freenode, snoonet, tmwirc, snyde);
    }
}
