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

package com.fusionx.lightirc.irc;

import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.events.lightirc.IOExceptionEvent;
import org.pircbotx.hooks.events.lightirc.IrcExceptionEvent;

import java.io.IOException;

public class LightThread extends Thread {
    @Getter(AccessLevel.PUBLIC)
    private final PircBotX bot;

    public LightThread(final PircBotX bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        try {
            bot.startBot();
        } catch (IOException e) {
            bot.getConfiguration().getListenerManager().dispatchEvent(new IOExceptionEvent<PircBotX>(bot, e));
        } catch (IrcException e) {
            bot.getConfiguration().getListenerManager().dispatchEvent(new IrcExceptionEvent<PircBotX>(bot, e));
        }
    }
}
