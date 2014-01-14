package com.fusionx.lightirc.loaders;

import com.fusionx.relay.Server;
import com.fusionx.relay.communication.ServerEventBus;
import com.fusionx.relay.event.Event;

import android.content.Context;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

public abstract class IRCLoader<T extends Event> extends Loader<List<T>> {

    private final ServerEventBus mBus;

    protected List<T> mEvents;

    private boolean mRegistered;

    public IRCLoader(final Context context, final Server server) {
        super(context);

        mBus = server.getServerEventBus();
    }

    @Override
    protected void onForceLoad() {
        mEvents = new ArrayList<>();
    }

    @Override
    protected void onStartLoading() {
        if (mEvents == null) {
            onForceLoad();

            if (!mRegistered) {
                mBus.register(this);
                mRegistered = true;
            }
        } else {
            deliverResult(mEvents);
            mEvents = new ArrayList<>(10);
        }
    }

    @Override
    protected void onReset() {
        if (mRegistered) {
            mBus.unregister(this);
            mRegistered = false;
        }

        mEvents = null;
    }
}