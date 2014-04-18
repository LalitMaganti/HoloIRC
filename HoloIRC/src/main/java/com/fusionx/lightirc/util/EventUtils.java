package com.fusionx.lightirc.util;

import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.event.channel.WorldUserEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.PartEvent;
import com.fusionx.relay.event.server.ServerEvent;

import android.content.Context;

public class EventUtils {

    private static final ImmutableList<? extends Class<? extends ServerEvent>>
            sServerIgnoreClasses = ImmutableList.of(JoinEvent.class, PartEvent.class);


    public static boolean shouldDisplayEvent(final Context context, final Event event) {
        if (event instanceof WorldUserEvent) {
            final WorldUserEvent worldUserEvent = (WorldUserEvent) event;
            if (worldUserEvent.isUserListChangeEvent()) {
                return !AppPreferences.getAppPreferences(context).isHideUserMessages();
            }
        } else if (event instanceof ServerEvent) {
            final ServerEvent serverEvent = (ServerEvent) event;
            return !sServerIgnoreClasses.contains(serverEvent.getClass());
        }
        return true;
    }
}