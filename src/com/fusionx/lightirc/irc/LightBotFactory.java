package com.fusionx.lightirc.irc;

import org.pircbotx.Channel;
import org.pircbotx.Configuration.BotFactory;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

public class LightBotFactory extends BotFactory {
    @Override
    public Channel createChannel(PircBotX bot, String name) {
        return new LightChannel(bot, bot.getUserChannelDao(), name);
    }

    @Override
    public User createUser(PircBotX bot, String nick) {
        return new LightUser(bot, bot.getUserChannelDao(), nick);
    }
}