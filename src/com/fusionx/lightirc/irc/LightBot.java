package com.fusionx.lightirc.irc;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class LightBot extends PircBotX {
    public String mBuffer = "";
    private String mTitle;

    public LightBot(Configuration configuration, final String title) {
        super(configuration);
    }

    public void appendToBuffer(String message) {
        mBuffer += message;
    }

    public String getBuffer() {
        return mBuffer;
    }

    public String getTitle() {
        return mTitle;
    }
}