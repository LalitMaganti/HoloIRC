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

import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.misc.Utils;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NoticeEvent;

public class ServerListener extends ListenerAdapter<LightBot> implements Listener<LightBot> {
    @Override
    public void onEvent(final Event<LightBot> event) {
        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            event.getBot().appendToBuffer(Utils.getOutputForEvent(event));
        }
    }
}