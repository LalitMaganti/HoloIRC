package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.irc.IOExceptionEvent;
import com.fusionx.lightirc.irc.IrcExceptionEvent;
import lombok.AccessLevel;
import lombok.Getter;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import java.io.IOException;

public class LightThread extends Thread {
    @Getter(AccessLevel.PUBLIC)
    private PircBotX bot;

    public LightThread(PircBotX bot) {
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
