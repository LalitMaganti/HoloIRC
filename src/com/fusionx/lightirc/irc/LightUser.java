package com.fusionx.lightirc.irc;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;

public class LightUser extends User {
    public LightUser(PircBotX bot, UserChannelDao dao, String nick) {
        super(bot, dao, nick);
    }

    public String getPrettyNick(Channel c) {
        if (c.getOps().contains(this)) {
            return "@" + getNick();
        } else if (c.getHalfOps().contains(this)) {
            return "half@" + getNick();
        } else if (c.getVoices().contains(this)) {
            return "+" + getNick();
        } else {
            return getNick();
        }
    }
}
