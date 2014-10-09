package com.fusionx.lightirc.util;

import com.google.common.collect.ImmutableSet;

import com.fusionx.lightirc.misc.AppPreferences;

import java.util.List;
import java.util.Set;

import co.fusionx.relay.dcc.event.file.DCCFileEvent;
import co.fusionx.relay.dcc.event.file.DCCFileProgressEvent;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.ChannelNameEvent;
import co.fusionx.relay.event.channel.ChannelWorldUserEvent;
import co.fusionx.relay.event.query.QueryClosedEvent;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;
import co.fusionx.relay.event.server.ServerEvent;
import co.fusionx.relay.event.server.StatusChangeEvent;

public class EventUtils {

    private static final Set<Class<? extends ServerEvent>> SERVER_IGNORE_EVENTS
            = ImmutableSet.of(JoinEvent.class, NewPrivateMessageEvent.class,
            StatusChangeEvent.class);

    private static final Set<Class<? extends ChannelEvent>> CHANNEL_IGNORE_EVENTS
            = ImmutableSet.of(ChannelNameEvent.class);

    private static final Set<Class<? extends DCCFileEvent>> DCC_FILE_IGNORE_EVENTS
            = ImmutableSet.of(DCCFileProgressEvent.class);

    private static final Set<Class<? extends QueryEvent>> QUERY_IGNORE_EVENTS
            = ImmutableSet.of(QueryClosedEvent.class);

    public static boolean shouldStoreEvent(final Event event) {
        if (event instanceof ChannelWorldUserEvent) {
            final ChannelWorldUserEvent channelWorldUserEvent = (ChannelWorldUserEvent) event;
            if (channelWorldUserEvent.isUserListChangeEvent()) {
                return !AppPreferences.getAppPreferences().shouldHideUserMessages();
            }

            // TODO - readd ignore list functionality back in here
        } else if (event instanceof ServerEvent) {
            final ServerEvent serverEvent = (ServerEvent) event;
            return !SERVER_IGNORE_EVENTS.contains(serverEvent.getClass());
        } else if (event instanceof ChannelEvent) {
            final ChannelEvent channelEvent = (ChannelEvent) event;
            return !CHANNEL_IGNORE_EVENTS.contains(channelEvent.getClass());
        } else if (event instanceof DCCFileEvent) {
            final DCCFileEvent fileEvent = (DCCFileEvent) event;
            return DCC_FILE_IGNORE_EVENTS.contains(fileEvent.getClass());
        } else if (event instanceof QueryEvent) {
            final QueryEvent queryEvent = (QueryEvent) event;
            return !QUERY_IGNORE_EVENTS.contains(queryEvent.getClass());
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