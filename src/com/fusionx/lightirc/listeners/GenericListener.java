package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.irc.IOExceptionEvent;
import com.fusionx.lightirc.irc.IrcExceptionEvent;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.PrivateActionEvent;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

public abstract class GenericListener extends ListenerAdapter<PircBotX> implements Listener<PircBotX> {
    @Override
    public void onEvent(Event event) throws Exception {
        if (event instanceof NickChangeEventPerChannel)
            onNickChangePerChannel((NickChangeEventPerChannel) event);
        else if (event instanceof QuitEventPerChannel)
            onQuitPerChannel((QuitEventPerChannel) event);
        else if (event instanceof PrivateActionEvent)
            onPrivateAction((PrivateActionEvent) event);
        else if (event instanceof IrcExceptionEvent)
            onIrcException((IrcExceptionEvent) event);
        else if (event instanceof IOExceptionEvent)
            onIOException((IOExceptionEvent) event);
        else
            super.onEvent(event);
    }

    protected abstract void onIOException(IOExceptionEvent<PircBotX> event);

    protected abstract void onIrcException(IrcExceptionEvent<PircBotX> event);

    protected abstract void onNickChangePerChannel(NickChangeEventPerChannel<PircBotX> event) throws Exception;

    protected abstract void onQuitPerChannel(QuitEventPerChannel<PircBotX> event) throws Exception;

    protected abstract void onPrivateAction(PrivateActionEvent<PircBotX> event) throws Exception;

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
