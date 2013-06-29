package com.fusionx.lightirc.irc;

import org.pircbotx.Configuration;
import org.pircbotx.InputParser;
import org.pircbotx.LightInputParser;
import org.pircbotx.PircBotX;

public class LightBotFactory extends Configuration.BotFactory {
    @Override
    public InputParser createInputParser(final PircBotX bot) {
        return new LightInputParser(bot);
    }
}
