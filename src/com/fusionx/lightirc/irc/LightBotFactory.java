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

import android.content.Context;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.Utils;
import lombok.AccessLevel;
import lombok.Setter;
import org.pircbotx.*;

public class LightBotFactory extends Configuration.BotFactory {
    @Setter(AccessLevel.PUBLIC)
    private Context applicationContext;

    @Override
    public InputParser createInputParser(final PircBotX bot) {
        return new LightInputParser(bot);
    }

    @Override
    public User createUser(final PircBotX bot, final String nick) {
        if (Utils.getThemeInt(applicationContext) == R.style.Light) {
            return new User(bot, bot.getUserChannelDao(), nick, 0);
        } else {
            return new User(bot, bot.getUserChannelDao(), nick, 255);
        }
    }
}