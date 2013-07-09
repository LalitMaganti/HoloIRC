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
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.lightirc.IOExceptionEvent;
import org.pircbotx.hooks.events.lightirc.IrcExceptionEvent;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

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
        } else
            super.onEvent(event);
    }

    protected abstract void onIOException(final IOExceptionEvent<PircBotX> event);

    protected abstract void onIrcException(final IrcExceptionEvent<PircBotX> event);

    protected abstract void onNickChangePerChannel(final NickChangeEventPerChannel<PircBotX> event);

    protected abstract void onQuitPerChannel(final QuitEventPerChannel<PircBotX> event);

    protected abstract void onBotJoin(final JoinEvent<PircBotX> event);

    protected abstract void onOtherUserJoin(final JoinEvent<PircBotX> event);

    protected abstract void onOtherUserPart(final PartEvent<PircBotX> event);

    void onUserPart(final PartEvent<PircBotX> event) {
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
}
