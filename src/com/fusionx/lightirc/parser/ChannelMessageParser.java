package com.fusionx.lightirc.parser;

import android.content.Context;
import android.content.Intent;
import com.fusionx.lightirc.irc.LightBot;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;

public class ChannelMessageParser extends IRCMessageParser {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String channelName = intent.getStringExtra("channel");
        String message = intent.getStringExtra("message");
        String serverName = intent.getStringExtra("serverName");
        LightBot bot = getService().getBot(serverName);

        if (message.startsWith("/")) {
            // TODO parse this string fully
            if (message.startsWith("/join")) {
                String channel = message.replace("/join ", "");
                // TODO - input validation
                bot.sendIRC().joinChannel(channel);
            } else if (message.startsWith("/me")) {
                String action = message.replace("/me ", "");
                // TODO - input validation
                bot.sendIRC().action(channelName, action);
                bot.getConfiguration().getListenerManager().dispatchEvent(new ActionEvent(bot, bot.getUserBot(), bot.getUserChannelDao().getChannel(channelName), action));
            } else if (message.startsWith("/nick")) {
                String newNick = message.replace("/nick ", "");
                bot.sendIRC().changeNick(newNick);
            } else {
                String bufferMessage = "Unknown command";
                //Dispatch event here
            }
        } else {
            bot.sendIRC().message(channelName, message);
            bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(bot, bot.getUserChannelDao().getChannel(channelName), bot.getUserBot(), message));
        }
    }
}