package com.fusionx.lightirc.listeners;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.lightirc.PartEvent;

public abstract class GenericListener extends ListenerAdapter<PircBotX> implements Listener<PircBotX> {

    protected abstract void onBotJoin(JoinEvent<PircBotX> event);

    protected abstract void onOtherUserJoin(JoinEvent<PircBotX> event);

    protected abstract void onOtherUserPart(PartEvent<PircBotX> event);

    @Override
    public void onJoin(final JoinEvent<PircBotX> event) {
        if (!((JoinEvent) event).getUser().getNick().equals(event.getBot().getUserBot().getNick())) {
            onOtherUserJoin(event);
        } else {
            onBotJoin(event);
        }
    }

    @Override
    public void onPart(final PartEvent<PircBotX> event) {
        if (!event.getUser().getNick().equals(event.getBot().getUserBot().getNick())) {
            onOtherUserPart(event);
        }
    }
}
