package com.fusionx.lightirc.misc;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.model.EventDecorator;

import android.content.Context;
import android.support.v4.util.LruCache;

import co.fusionx.relay.event.Event;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class EventCache extends LruCache<Event, EventDecorator> {

    public static final int EVENT_CACHE_MAX_SIZE = 300;

    private final IRCEventToStringConverter mConverter;
    private final boolean mForDarkBackground;

    public EventCache(final Context context, final boolean darkBackground) {
        super(EVENT_CACHE_MAX_SIZE);

        mConverter = IRCEventToStringConverter.getConverter(context);
        mForDarkBackground = darkBackground;

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
            return mConverter.getEventDecorator(key, mForDarkBackground);
        }
    }
}