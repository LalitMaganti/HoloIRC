package com.fusionx.lightirc.misc;

import android.os.Binder;

import com.fusionx.lightirc.services.IRCService;

public class IRCBinder extends Binder {
    private final IRCService service;
    
    public IRCBinder(IRCService service) {
        super();
        this.service = service;
    }
    
    public IRCService getService() {
        // Return this instance of IRCService so clients can call public methods
        return service;
    }
}
