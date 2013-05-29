package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.irc.LightBot;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PartEvent;

public abstract class GenericListener extends ListenerAdapter<LightBot> implements Listener<LightBot> {

    protected abstract void userJoin(JoinEvent<LightBot> event);

    protected abstract void otherUserJoin(JoinEvent<LightBot> event);

    protected abstract void part(PartEvent<LightBot> event);

    @Override
    public void onJoin(final JoinEvent<LightBot> event) {
        if (!((JoinEvent) event).getUser().getNick().equals(event.getBot().getNick())) {
            otherUserJoin(event);
        } else {
            userJoin(event);
        }
    }

    @Override
    public void onPart(final PartEvent<LightBot> event) {
        if (!event.getUser().getNick().equals(event.getBot().getNick())) {
            part(event);
        }
    }
}
