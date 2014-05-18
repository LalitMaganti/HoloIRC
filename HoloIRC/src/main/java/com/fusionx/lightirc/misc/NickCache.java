package com.fusionx.lightirc.misc;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.model.NickColour;
import com.fusionx.relay.nick.Nick;

import android.support.v4.util.LruCache;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

public class NickCache extends LruCache<Nick, NickColour> {

    public static final int NICK_CACHE_MAX_SIZE = 1000;

    private static NickCache mNickCache;

    public NickCache() {
        super(NICK_CACHE_MAX_SIZE);

        // If the preferences change then clear the cache
        getBus().register(new Object() {
            @Subscribe
            public void onEvent(final OnPreferencesChangedEvent event) {
                evictAll();
            }
        }, 300);
    }

    public static NickCache getNickCache() {
        if (mNickCache == null) {
            mNickCache = new NickCache();
        }
        return mNickCache;
    }

    @Override
    protected NickColour create(Nick key) {
        synchronized (this) {
            return new NickColour(key);
        }
    }
}