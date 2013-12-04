package com.fusionx.lightirc.util;

import com.fusionx.androidirclibrary.util.IRCUtils;
import com.fusionx.lightirc.RobolectricGradleTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricGradleTestRunner.class)
public class IRCUtilsTest {

    @Test
    public void checkNicks() throws Exception {
        final String nick = IRCUtils.getNickFromRaw("ikonia!~irc@unaffiliated/ikonia");
        Assert.assertEquals(nick, "ikonia");
    }

    @Test
    public void checkHostname() throws Exception {
        final String hostname = IRCUtils.getHostNameFromRaw("ikonia!~irc@unaffiliated/ikonia");
        Assert.assertEquals(hostname, "unaffiliated/ikonia");
    }
}