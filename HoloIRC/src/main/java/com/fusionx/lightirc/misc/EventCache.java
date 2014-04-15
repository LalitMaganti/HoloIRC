package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.relay.event.Event;

import android.support.v4.util.LruCache;

import de.greenrobot.event.EventBus;

public class EventCache extends LruCache<Event, EventDecorator> {

    public static final int EVENT_CACHE_MAX_SIZE = 128;

    public EventCache() {
        super(EVENT_CACHE_MAX_SIZE);

        // If the preferences change then clear the cache
        EventBus.getDefault().register(new Object() {
            @SuppressWarnings("unused")
            public void onEvent(final OnPreferencesChangedEvent event) {
                evictAll();
            }
        }, 300);
    }
}