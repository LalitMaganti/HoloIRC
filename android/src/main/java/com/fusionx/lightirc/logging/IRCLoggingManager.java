package com.fusionx.lightirc.logging;

import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.EventUtils;
import co.fusionx.relay.Server;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.logging.LoggingManager;
import co.fusionx.relay.logging.LoggingPreferences;

public class IRCLoggingManager extends LoggingManager {

    private final IRCService mIRCService;

    public IRCLoggingManager(final IRCService service, final LoggingPreferences preferences) {
        super(preferences);

        mIRCService = service;
    }

    @Override
    public CharSequence getMessageFromEvent(final Server server, final Event event) {
        EventDecorator decorator = mIRCService.getEventCache(server).get(event);
        return decorator.getMessage();
    }

    @Override
    protected boolean shouldLogEvent(final Event event) {
        return EventUtils.shouldStoreEvent(event);
    }
}