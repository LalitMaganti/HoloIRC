package app.holoirc.logging;

import app.holoirc.model.EventDecorator;
import app.holoirc.service.IRCService;
import app.holoirc.util.EventUtils;

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
        EventDecorator decorator = IRCService.getEventCache(server, false).get(event);
        return decorator.getMessage();
    }

    @Override
    protected boolean shouldLogEvent(final Event event) {
        return EventUtils.shouldStoreEvent(event);
    }
}