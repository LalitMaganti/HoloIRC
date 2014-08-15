package com.fusionx.lightirc.misc;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.relay.event.Event;

import android.content.Context;
import android.support.v4.util.LruCache;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class EventCache extends LruCache<Event, EventDecorator> {

    public static final int EVENT_CACHE_MAX_SIZE = 300;

    private final IRCEventToStringConverter mConverter;

    public EventCache(final Context context) {
        super(EVENT_CACHE_MAX_SIZE);

        mConverter = IRCEventToStringConverter.getConverter(context);

        // If the preferences change then clear the cache
        getBus().register(new Object() {
            @Subscribe
            public void onEvent(final OnPreferencesChangedEvent event) {
                evictAll();
            }
        }, 300);
    }

    @Override
    protected EventDecorator create(final Event key) {
        synchronized (mConverter) {
            return mConverter.getEventDecorator(key);
        }
    }
}