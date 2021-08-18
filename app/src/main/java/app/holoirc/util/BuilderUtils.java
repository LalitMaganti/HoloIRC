package app.holoirc.util;

import java.util.Arrays;
import java.util.List;

import co.fusionx.relay.base.ServerConfiguration;
import co.fusionx.relay.misc.NickStorage;

class BuilderUtils {

    public static List<ServerConfiguration.Builder> getFirstTimeBuilderList() {
        final ServerConfiguration.Builder libera = new ServerConfiguration.Builder();
        libera.setTitle("Libera.Chat").setUrl("irc.libera.chat").setPort(6667).setSsl(false);
        libera.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        libera.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ServerConfiguration.Builder snoonet = new ServerConfiguration.Builder();
        snoonet.setTitle("Snoonet").setUrl("irc.snoonet.org").setPort(6667).setSsl(false);
        snoonet.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        snoonet.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        final ServerConfiguration.Builder techtronix = new ServerConfiguration.Builder();
        techtronix.setTitle("Techtronix").setUrl("irc.techtronix.net").setPort(6667)
                .setSsl(false);
        techtronix.setNickStorage(new NickStorage("HoloIRCUser", "", ""));
        techtronix.setRealName("HoloIRCUser").setNickChangeable(true).setServerUserName("holoirc");

        return Arrays.asList(libera, snoonet, techtronix);
    }
}
