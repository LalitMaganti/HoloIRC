package com.fusionx.lightirc.loaders;

import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.user.UserEvent;
import com.squareup.otto.Subscribe;

import android.content.Context;

import java.util.ArrayList;

public class UserLoader extends IRCLoader<UserEvent> {

    public PrivateMessageUser mUser;

    public UserLoader(Context context, final Server server, final PrivateMessageUser user) {
        super(context, server);

        mUser = user;
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