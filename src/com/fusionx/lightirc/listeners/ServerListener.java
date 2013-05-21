package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.misc.EventOutput;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NoticeEvent;

public class ServerListener extends IRCListener {
    @Override
    public void onEvent(final Event<LightBot> event) {
        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            event.getBot().appendToBuffer(EventOutput.getOutputForEvent(event));
        }
    }
}