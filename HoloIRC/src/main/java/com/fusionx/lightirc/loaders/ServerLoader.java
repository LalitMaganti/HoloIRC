package com.fusionx.lightirc.loaders;

import com.fusionx.relay.Server;
import com.fusionx.relay.communication.ServerEventBus;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.ServerEvent;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class ServerLoader extends Loader<List<ServerEvent>> {

    public List<ServerEvent> mEvents;

    public ServerEventBus mBus;

    public Server mServer;

    public ServerLoader(final Context context, final Server server) {
        super(context);

        mBus = server.getServerEventBus();
        mServer = server;
    }

    @Override
    protected void onForceLoad() {
        deliverResult(mServer.getBuffer());

        mEvents = new ArrayList<>(10);
    }

    @Override
    protected void onStartLoading() {
        if (mEvents == null) {
            onForceLoad();
            mBus.register(this);
        } else {
            deliverResult(mEvents);
            mEvents = new ArrayList<>(10);
        }
    }

    @Override
    protected void onReset() {
        mBus.unregister(this);
        mEvents = null;
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