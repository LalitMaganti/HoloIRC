/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.listeners;

import android.content.Context;
import com.fusionx.lightirc.misc.Utils;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.*;

abstract class GenericListener extends ListenerAdapter<PircBotX> implements Listener<PircBotX> {
    final Context applicationContext;

    GenericListener(final Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onEvent(final Event<PircBotX> event) throws Exception {
        if (event instanceof NickChangeEventPerChannel)
            onNickChangePerChannel((NickChangeEventPerChannel<PircBotX>) event);
        else if (event instanceof QuitEventPerChannel) {
            if (Utils.isMessagesFromChannelShown(applicationContext)) {
                onQuitPerChannel((QuitEventPerChannel<PircBotX>) event);
            }
        } else if (event instanceof IrcExceptionEvent)
            onIrcException((IrcExceptionEvent<PircBotX>) event);
        else if (event instanceof IOExceptionEvent)
            onIOException((IOExceptionEvent<PircBotX>) event);
        else if (event instanceof MotdEvent) {
            if (Utils.isMotdAllowed(applicationContext)) {
                onMotd((MotdEvent<PircBotX>) event);
            }
        } else if (event instanceof NickInUseEvent) {
            onNickInUse((NickInUseEvent<PircBotX>) event);
        } else
            super.onEvent(event);
    }

    protected abstract void onIOException(final IOExceptionEvent<PircBotX> event);

    protected abstract void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event);

    protected abstract void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event);

    protected abstract void onBotJoin(final JoinEvent<PircBotX> event);

    protected abstract void onOtherUserJoin(final JoinEvent<PircBotX> event);

    protected abstract void onOtherUserPart(final PartEvent<PircBotX> event);


    void onUserPart(final PartEvent<PircBotX> event) {
    }

    @Override
    public void onMode(final ModeEvent<PircBotX> event) {
        if (Utils.isMessagesFromChannelShown(applicationContext)) {
            onChannelMessage(event, event.getChannel());
        }
    }

    @Override
    public void onJoin(final JoinEvent<PircBotX> event) {
        if (!((JoinEvent) event).getUser().getNick().equals(event.getBot().getUserBot().getNick())) {
            if (Utils.isMessagesFromChannelShown(applicationContext)) {
                onOtherUserJoin(event);
            }
        } else {
            onBotJoin(event);
        }
    }

    @Override
    public void onPart(final PartEvent<PircBotX> event) {
        if (!event.getUser().getNick().equals(event.getBot().getUserBot().getNick())) {
            if (Utils.isMessagesFromChannelShown(applicationContext)) {
                onOtherUserPart(event);
            }
        } else {
            onUserPart(event);
        }
    }

    public void onNickInUse(NickInUseEvent<PircBotX> event) {
        onServerMessage(event);
    }

    protected void onIrcException(final IrcExceptionEvent<PircBotX> event) {
        onServerMessage(event);
    }

    @Override
    public void onMotd(final MotdEvent<PircBotX> event) {
        onServerMessage(event);
    }

    @Override
    public void onUnknown(final UnknownEvent<PircBotX> event) {
        onServerMessage(event);
    }

    @Override
    public void onTopic(final TopicEvent<PircBotX> event) {
        onChannelMessage(event, event.getChannel());
    }

    @Override
    public void onAction(final ActionEvent<PircBotX> event) {
        if (event.getChannel() != null) {
            onChannelMessage(event, event.getChannel());
        } else {
            onPrivateEvent(event, event.getUser(), event.getAction());
        }
    }

    @Override
    public void onMessage(final MessageEvent<PircBotX> event) {
        onChannelMessage(event, event.getChannel());
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent<PircBotX> event) {
        onPrivateEvent(event, event.getUser(), event.getMessage());
    }

    abstract void onPrivateEvent(Event<PircBotX> event, User user, String message);

    abstract void onServerMessage(Event<PircBotX> event);

    abstract void onChannelMessage(Event<PircBotX> event, Channel channel);

    abstract void onUserMessage(Event<PircBotX> event, User user);
}
