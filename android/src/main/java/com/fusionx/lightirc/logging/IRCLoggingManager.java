package com.fusionx.lightirc.logging;

import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.EventUtils;

import co.fusionx.relay.core.Session;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.logging.LoggingManager;
import co.fusionx.relay.logging.LoggingSettingsProvider;

public class IRCLoggingManager extends LoggingManager {

    public IRCLoggingManager(final LoggingSettingsProvider preferences) {
        super(preferences);
    }

    @Override
    public CharSequence getMessageFromEvent(final Session connection, final Event event) {
        final EventCache eventCache = IRCService.getEventCache(connection);
        final EventDecorator decorator = eventCache.get(event);
        return decorator.getMessage();
    }

    @Override
    protected boolean shouldLogEvent(final Event event) {
        return EventUtils.shouldStoreEvent(event);
    }
}