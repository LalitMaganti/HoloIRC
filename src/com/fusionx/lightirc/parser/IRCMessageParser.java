package com.fusionx.lightirc.parser;

import android.content.BroadcastReceiver;
import com.fusionx.lightirc.services.IRCService;

public abstract class IRCMessageParser extends BroadcastReceiver {
    private IRCService mService;

    public IRCService getService() {
        return mService;
    }

    public void setService(IRCService service) {
        mService = service;
    }
}
