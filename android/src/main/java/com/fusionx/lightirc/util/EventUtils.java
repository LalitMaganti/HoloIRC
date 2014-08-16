package com.fusionx.lightirc.util;

import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.misc.AppPreferences;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.ChannelNameEvent;
import co.fusionx.relay.event.channel.ChannelWorldUserEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;
import co.fusionx.relay.event.server.PartEvent;
import co.fusionx.relay.event.server.PrivateMessageClosedEvent;
import co.fusionx.relay.event.server.ServerEvent;
import co.fusionx.relay.event.server.StatusChangeEvent;

import java.util.List;

public class EventUtils {

    private static final List<Class<? extends ServerEvent>> sServerIgnoreClasses
            = ImmutableList.of(JoinEvent.class, PartEvent.class, NewPrivateMessageEvent.class,
            PrivateMessageClosedEvent.class, StatusChangeEvent.class);

    private static final List<Class<? extends ChannelEvent>> sChannelIgnoreClasses
            = ImmutableList.of(ChannelNameEvent.class);

    public static boolean shouldStoreEvent(final Event event) {
        if (event instanceof ChannelWorldUserEvent) {
            final ChannelWorldUserEvent channelWorldUserEvent = (ChannelWorldUserEvent) event;
            if (channelWorldUserEvent.isUserListChangeEvent()) {
                return !AppPreferences.getAppPreferences().shouldHideUserMessages();
            }
        } else if (event instanceof ServerEvent) {
            final ServerEvent serverEvent = (ServerEvent) event;
            return !sServerIgnoreClasses.contains(serverEvent.getClass());
        } else if (event instanceof ChannelEvent) {
            final ChannelEvent channelEvent = (ChannelEvent) event;
            return !sChannelIgnoreClasses.contains(channelEvent.getClass());
        }
        return true;
    }

    public static <T extends Event> T getLastStorableEvent(final List<T> eventList) {
        for (int i = eventList.size() - 1; i >= 0; i--) {
            final T event = eventList.get(i);
            if (shouldStoreEvent(event)) {
                return event;
            }
        }
        return eventList.get(eventList.size() - 1);
    }
}