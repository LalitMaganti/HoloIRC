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