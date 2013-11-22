package com.fusionx.lightirc.irc.parser;

import android.os.Handler;

import com.fusionx.lightirc.RobolectricGradleTestRunner;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.fusionx.lightirc.irc.connection.ConnectionWrapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(RobolectricGradleTestRunner.class)
public class ServerLineParserTest {
    private ServerLineParser parser;

    @Before
    public void setUp() throws Exception {
        final Handler handler = new Handler();
        ServerConfiguration.Builder builder = new ServerConfiguration.Builder();
        final ConnectionWrapper wrapper = new ConnectionWrapper(builder.build(),
                Robolectric.getShadowApplication().getApplicationContext(), handler);
        final Server server = new Server("", wrapper, Robolectric.getShadowApplication()
                .getApplicationContext(), handler);
        parser = new ServerLineParser(server);
    }

    @Test
    public void shouldUpdateResultsWhenButtonIsClicked() throws Exception {
    }
}
