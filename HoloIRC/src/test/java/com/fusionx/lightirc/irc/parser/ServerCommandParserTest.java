package com.fusionx.lightirc.irc.parser;

import android.content.Context;
import android.os.Handler;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.irc.AppUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.connection.ConnectionWrapper;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.util.MiscUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ServerCommandParserTest {
    private ServerCommandParser parser;
    private Context context;
    private Server server;

    @Before
    public void setUp() throws Exception {
        final Handler handler = new Handler();
        context = Robolectric.getShadowApplication().getApplicationContext();
        ServerConfiguration.Builder builder = new ServerConfiguration.Builder();
        final ConnectionWrapper wrapper = new ConnectionWrapper(builder.build(), context, handler);
        server = new Server("test", wrapper, context, handler);
        MessageSender.getSender("test").getBus();
        final OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
        server.setupUserChannelInterface(writer);
        parser = new ServerCommandParser(context, new ServerLineParser(server));
        server.setUser(new AppUser("tilal6991", server.getUserChannelInterface()));
        server.getUserChannelInterface().coupleUserAndChannel(server.getUser(),
                server.getUserChannelInterface().getChannel("'holoirc"));
    }

    @Test
    public void testParseMode() {
        final String testOne = ":tilal6991!~tilal6991@90.199.59.167 MODE #holoirc -bbbb " +
                "*!*@unaffiliated/rly *!*@176.96.167.187 $a:VOTProductions *!*@184.154.157.24";
        ArrayList<String> strings = MiscUtils.splitRawLine(testOne, true);
        final ChannelEvent event = (ChannelEvent) parser.parseCommand(strings, testOne, false);
        final String expected = String.format(context.getString(R.string.parser_mode_changed),
                "-bbbb", "*!*@unaffiliated/rly *!*@176.96.167.187 $a:VOTProductions *!*@184.154" +
                ".157.24", server.getUser().getPrettyNick(server.getUserChannelInterface()
                .getChannel("'holoirc")));
        assertEquals(expected, event.message);
    }
}