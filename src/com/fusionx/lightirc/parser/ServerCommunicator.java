package com.fusionx.lightirc.parser;

import android.content.Context;
import android.os.AsyncTask;
import com.fusionx.lightirc.misc.Utils;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.UnknownEvent;

public class ServerCommunicator {
    public static void sendJoin(final PircBotX bot, final String channelName) {
        final AsyncTask<Void, Void, Void> sendJoin = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                bot.sendIRC().joinChannel(channelName);
                return null;
            }
        };
        sendJoin.execute();
    }

    public static void sendMessageToChannel(final PircBotX bot, final String channelName, final String message) {
        final AsyncTask<Void, Void, Void> sendMessage = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final Channel channel = bot.getUserChannelDao().getChannel(channelName);
                bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent<PircBotX>(bot,
                        channel, bot.getUserBot(), message));
                channel.send().message(message);
                return null;
            }
        };
        sendMessage.execute();
    }

    public static void sendActionToChannel(final PircBotX bot, final String channelName, final String action) {
        final AsyncTask<Void, Void, Void> sendAction = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                bot.getConfiguration().getListenerManager().dispatchEvent(new ActionEvent<PircBotX>(bot, bot.getUserBot(),
                        bot.getUserChannelDao().getChannel(channelName), action));
                bot.getUserChannelDao().getChannel(channelName).send().action(action);
                return null;
            }
        };
        sendAction.execute();
    }

    public static void sendMessageToUser(final PircBotX bot, final String userNick, final String message) {
        final AsyncTask<Void, Void, Void> sendMessage = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final User user = bot.getUserChannelDao().getUser(userNick);
                bot.getConfiguration().getListenerManager()
                        .dispatchEvent(new PrivateMessageEvent<PircBotX>(bot, user, message, true));
                user.send().message(message);
                return null;
            }
        };
        sendMessage.execute();
    }

    public static void sendActionToUser(final PircBotX bot, final String userNick, final String action) {
        final AsyncTask<Void, Void, Void> sendAction = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final User user = bot.getUserChannelDao().getUser(userNick);
                bot.getConfiguration().getListenerManager()
                        .dispatchEvent(new ActionEvent<PircBotX>(bot, user, null, action));
                user.send().action(action);
                return null;
            }
        };
        sendAction.execute();
    }

    public static void sendNickChange(final PircBotX bot, final String newNick) {
        final AsyncTask<Void, Void, Void> changeNick = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                bot.sendIRC().changeNick(newNick);
                return null;
            }
        };
        changeNick.execute();
    }

    public static void sendPart(final PircBotX bot, final String channelName, final Context applicationContext) {
        final Channel channel = bot.getUserChannelDao().getChannel(channelName);
        sendPart(channel, applicationContext);
    }

    public static void sendPart(final Channel channel, final Context applicationContext) {
        final AsyncTask<Void, Void, Void> sendPart = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                channel.send().part(Utils.getPartReason(applicationContext));
                return null;
            }
        };
        sendPart.execute();
    }


    public static void sendClosePrivateMessage(final PircBotX bot, final String userNick) {
        sendClosePrivateMessage(bot.getUserChannelDao().getUser(userNick));
    }

    public static void sendClosePrivateMessage(final User user) {
        final AsyncTask<Void, Void, Void> sendPart = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                user.closePrivateMessage();
                return null;
            }
        };
        sendPart.execute();
    }

    public static void sendUnknownEvent(final PircBotX bot, final String event) {
        final AsyncTask<Void, Void, Void> unknownEvent = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                bot.getConfiguration().getListenerManager().dispatchEvent(new UnknownEvent<PircBotX>(bot, event));
                return null;
            }
        };
        unknownEvent.execute();
    }
}
