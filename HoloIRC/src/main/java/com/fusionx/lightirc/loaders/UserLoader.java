package com.fusionx.lightirc.loaders;

import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.communication.ServerEventBus;
import com.fusionx.relay.event.user.UserEvent;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class UserLoader extends Loader<List<UserEvent>> {

    public List<UserEvent> mEvents;

    public ServerEventBus mBus;

    public PrivateMessageUser mUser;

    public UserLoader(Context context, final Server server, final PrivateMessageUser user) {
        super(context);

        mBus = server.getServerEventBus();
        mUser = user;
    }

    @Override
    protected void onForceLoad() {
        deliverResult(mUser.getBuffer());

        mEvents = new ArrayList<>();
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
    public void onPrivateEvent(final UserEvent event) {
        if (event.user.getNick().equals(mUser.getNick())) {
            mEvents.add(event);

            if (isStarted()) {
                deliverResult(mEvents);
                mEvents = new ArrayList<>(10);
            }
        }
    }
}