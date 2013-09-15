package com.fusionx.lightirc.communication;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

class IRCBus extends Bus {
    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final MessageSender mSender;
    private int registeredCount;

    public IRCBus(final MessageSender sender) {
        super(ThreadEnforcer.ANY);
        mSender = sender;
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

    @Override
    public void register(Object object) {
        super.register(object);

        ++registeredCount;
    }

    @Override
    public void unregister(Object object) {
        super.unregister(object);

        --registeredCount;
        if (registeredCount == 0) {
            mSender.removeSender();
        }
    }
}