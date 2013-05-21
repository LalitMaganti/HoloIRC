package com.fusionx.lightirc.irc;

import java.util.HashMap;

public class LightPircBotXManager extends HashMap<String, LightBot> {
    private static final long serialVersionUID = 2426166268063489300L;

    public void disconnectAll() {
        for (LightBot bot : values()) {
            bot.shutdown();
        }
    }
}