package com.fusionx.lightirc.loaders;

import com.fusionx.relay.Server;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.ServerEvent;
import com.squareup.otto.Subscribe;

import android.content.Context;

import java.util.ArrayList;

public class ServerLoader extends IRCLoader<ServerEvent> {

    public Server mServer;

    public ServerLoader(final Context context, final Server server) {
        super(context, server);

        mServer = server;
    }

    // Subscription methods
    @Subscribe
    public void onServerEvent(final ServerEvent event) {
        if (!(event instanceof JoinEvent) && !(event instanceof PartEvent)) {
            mEvents.add(event);

            if (isStarted()) {
                deliverResult(mEvents);
                mEvents = new ArrayList<>(10);
            }
        }
    }
}