package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.services.IRCService;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

public abstract class IRCListener extends ListenerAdapter<LightBot> implements
        Listener<LightBot> {
    private IRCService mService;

    protected IRCService getService() {
        return mService;
    }

    public void setService(IRCService service) {
        mService = service;
    }
}