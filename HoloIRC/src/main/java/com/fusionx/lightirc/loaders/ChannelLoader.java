package com.fusionx.lightirc.loaders;

import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.Channel;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.event.channel.MentionEvent;
import com.fusionx.relay.event.channel.NameEvent;
import com.fusionx.relay.event.channel.WorldUserEvent;
import com.squareup.otto.Subscribe;

import android.content.Context;

import java.util.ArrayList;

public class ChannelLoader extends IRCLoader<ChannelEvent> {

    private static ImmutableList<? extends Class<? extends ChannelEvent>> sClasses = ImmutableList.of
            (NameEvent.class, MentionEvent.class);

    private Channel mChannel;

    public ChannelLoader(Context context, final Server server, final Channel channel) {
        super(context, server);

        mChannel = channel;
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