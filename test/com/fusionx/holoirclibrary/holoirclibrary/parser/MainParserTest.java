package com.fusionx.irc.holoirclibrary.parser;

import com.fusionx.irc.holoirclibrary.Server;
import com.fusionx.irc.holoirclibrary.constants.EventDestination;
import com.fusionx.irc.holoirclibrary.enums.ServerEventType;
import com.fusionx.irc.holoirclibrary.events.Event;
import com.fusionx.irc.holoirclibrary.interfaces.CommonInterface;
import org.junit.Test;

public class MainParserTest {
    final MainParser parser = new MainParser();

    public MainParserTest() {
        parser.mCommonInterface = new CommonInterface() {
            @Override
            public void sendBroadcast(Event event) {
                // Nothing to see here
            }

            @Override
            public Server getServer() {
                return null;
            }
        };
    }

    @Test
    public void testParseLine() throws Exception {
        final Event e = parser.parseLine(":hubbard.freenode.net 253 tilal 10 :unknown connection(s)");
        assertEquals(e.getMessage(), "10 unknown connection(s)");
        assertEquals(e.getDestination(), EventDestination.Server);
        assertEquals(e.getType(), ServerEventType.Generic);
    }
}
