package com.fusionx.lightirc.misc;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.relay.event.Event;

import android.support.v4.util.LruCache;

import de.greenrobot.event.EventBus;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class EventCache extends LruCache<Event, EventDecorator> {

    public static final int EVENT_CACHE_MAX_SIZE = 128;

    public EventCache() {
        super(EVENT_CACHE_MAX_SIZE);

        // If the preferences change then clear the cache
        getBus().register(new Object() {
            @Subscribe
            public void onEvent(final OnPreferencesChangedEvent event) {
                evictAll();
            }
        }, 300);
    }
}