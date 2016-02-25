package com.fusionx.lightirc.event;

import com.fusionx.lightirc.service.IRCService;

import java.lang.ref.WeakReference;

public class OnServiceConnectionStateChanged {
    private final WeakReference<IRCService> mService;

    public OnServiceConnectionStateChanged(IRCService service) {
        mService = new WeakReference<>(service);
    }

    public IRCService getService() {
        return mService.get();
    }
}
