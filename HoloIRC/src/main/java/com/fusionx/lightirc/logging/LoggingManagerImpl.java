package com.fusionx.lightirc.logging;

import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.util.EventUtils;
import com.fusionx.lightirc.util.MessageConversionUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.logging.LoggingManager;
import com.fusionx.relay.logging.LoggingPreferences;

public class LoggingManagerImpl extends LoggingManager {

    private final MessageConversionUtils mConverter;

    private final IRCService mIRCService;

    public LoggingManagerImpl(final IRCService service, final LoggingPreferences preferences) {
        super(preferences);

        mIRCService = service;
        mConverter = MessageConversionUtils.getConverter(service);
    }

    @Override
    public CharSequence getMessageFromEvent(final Server server, final Event event) {
        EventDecorator decorator = mIRCService.getEventCache(server).get(event);
        if (decorator == null) {
            decorator = mConverter.getEventDecorator(event);
            mIRCService.getEventCache(server).put(event, decorator);
        }
        return decorator.getMessage();
    }

    @Override
    protected boolean shouldLogEvent(final Event event) {
        return EventUtils.shouldDisplayEvent(mIRCService, event);
    }
}