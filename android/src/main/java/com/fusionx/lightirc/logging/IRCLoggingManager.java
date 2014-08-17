package com.fusionx.lightirc.logging;

import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.EventUtils;

import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.logging.LoggingManager;
import co.fusionx.relay.logging.LoggingPreferences;

public class IRCLoggingManager extends LoggingManager {

    public IRCLoggingManager(final LoggingPreferences preferences) {
        super(preferences);
    }

    @Override
    public CharSequence getMessageFromEvent(final Server server, final Event event) {
        EventDecorator decorator = IRCService.getEventCache(server).get(event);
        return decorator.getMessage();
    }

    @Override
    protected boolean shouldLogEvent(final Event event) {
        return EventUtils.shouldStoreEvent(event);
    }
}