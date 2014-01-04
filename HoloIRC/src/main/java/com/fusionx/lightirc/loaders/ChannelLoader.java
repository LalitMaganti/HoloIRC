package com.fusionx.lightirc.loaders;

import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.communication.ServerEventBus;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.event.channel.MentionEvent;
import com.fusionx.relay.event.channel.NameEvent;
import com.fusionx.relay.event.channel.WorldUserEvent;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class ChannelLoader extends Loader<List<ChannelEvent>> {

    public static ImmutableList<? extends Class<? extends ChannelEvent>> sClasses = ImmutableList.of
            (NameEvent.class, MentionEvent.class);

    public List<ChannelEvent> mEvents;

    public ServerEventBus mBus;

    public Channel mChannel;

    public ChannelLoader(Context context, final Server server, final Channel channel) {
        super(context);

        mBus = server.getServerEventBus();
        mChannel = channel;
    }

    @Override
    protected void onForceLoad() {
        deliverResult(mChannel.getBuffer());

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
    public void onChannelMessage(final ChannelEvent event) {
        if (event.channelName.equals(mChannel.getName()) && !(sClasses
                .contains(event.getClass()))) {
            if (!(event instanceof WorldUserEvent && AppPreferences.hideUserMessages)) {
                mEvents.add(event);

                if (isStarted()) {
                    deliverResult(mEvents);
                    mEvents = new ArrayList<>(10);
                }
            }
        }
    }
}