package com.fusionx.lightirc.util;

import com.fusionx.relay.configuration.ParcelableConnectionConfiguration;
import com.fusionx.relay.core.ParcelableNickProvider;

import java.util.Arrays;
import java.util.List;

class BuilderUtils {

    public static List<ParcelableConnectionConfiguration.Builder> getFirstTimeBuilderList() {
        final ParcelableConnectionConfiguration.Builder freenode
                = new ParcelableConnectionConfiguration.Builder();
        freenode.setTitle("Freenode").setUrl("chat.freenode.net").setPort(6667).setSsl(false);
        freenode.setNickStorage(new ParcelableNickProvider("HoloIRCUser", "", ""));
        freenode.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ParcelableConnectionConfiguration.Builder snoonet
                = new ParcelableConnectionConfiguration.Builder();
        snoonet.setTitle("Snoonet").setUrl("irc.snoonet.org").setPort(6667).setSsl(false);
        snoonet.setNickStorage(new ParcelableNickProvider("HoloIRCUser", "", ""));
        snoonet.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ParcelableConnectionConfiguration.Builder tmwirc
                = new ParcelableConnectionConfiguration.Builder();
        tmwirc.setTitle("Techman's World IRC").setUrl("irc.techmansworld.com").setPort(6667)
                .setSsl(false);
        tmwirc.setNickStorage(new ParcelableNickProvider("HoloIRCUser", "", ""));
        tmwirc.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        return Arrays.asList(freenode, snoonet, tmwirc);
    }
}
