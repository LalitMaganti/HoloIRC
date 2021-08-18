package app.holoirc.misc;

import com.fusionx.bus.Subscribe;
import app.holoirc.event.OnPreferencesChangedEvent;
import app.holoirc.model.NickColour;

import androidx.collection.LruCache;

import app.holoirc.util.MiscUtils;
import co.fusionx.relay.base.Nick;

public class NickCache extends LruCache<Nick, NickColour> {

    public static final int NICK_CACHE_MAX_SIZE = 1000;

    private static NickCache mNickCache;

    public NickCache() {
        super(NICK_CACHE_MAX_SIZE);

        // If the preferences change then clear the cache
        MiscUtils.getBus().register(new Object() {
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