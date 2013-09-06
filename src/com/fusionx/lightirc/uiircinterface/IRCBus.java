package com.fusionx.lightirc.uiircinterface;

import android.os.Handler;
import android.os.Looper;

import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.event.ServerEvent;
import com.fusionx.lightirc.irc.event.UserEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class IRCBus extends Bus {
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public IRCBus(final ThreadEnforcer enforcer) {
        super(enforcer);
    }

    public void post(final Server server, final ServerEvent event) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                server.onServerEvent(event);
            }
        });
        post(event);
    }

    public void post(final Channel channel, final ChannelEvent event) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                channel.onChannelEvent(event);
            }
        });
        post(event);
    }

    public void post(final PrivateMessageUser user, final UserEvent event) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                user.onUserEvent(event);
            }
        });
        post(event);
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