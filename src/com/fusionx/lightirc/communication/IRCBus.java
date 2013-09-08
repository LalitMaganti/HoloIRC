package com.fusionx.lightirc.communication;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class IRCBus extends Bus {
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public IRCBus(final ThreadEnforcer enforcer) {
        super(enforcer);
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    IRCBus.super.post(event);
                }
            });
        }
    }
}