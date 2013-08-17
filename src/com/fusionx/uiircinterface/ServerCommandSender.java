package com.fusionx.uiircinterface;

import android.content.Context;
import android.os.AsyncTask;

import com.fusionx.irc.Channel;
import com.fusionx.irc.Server;
import com.fusionx.irc.User;
import com.fusionx.lightirc.misc.Utils;

public class ServerCommandSender {
    public static void sendJoin(final Server server, final String channelName) {
        final AsyncTask<Void, Void, Void> sendJoin = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().joinChannel(channelName);
                return null;
            }
        };
        sendJoin.execute();
    }

    public static void sendMessageToChannel(final Server server, final String channelName,
                                            final String message) {
        final AsyncTask<Void, Void, Void> sendMessage = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final Channel channel = server.getUserChannelInterface().getChannel(channelName);
                channel.getWriter().sendMessage(message);

                MessageSender.getSender(server.getTitle()).sendAppUserMessageToChannel(channel,
                        server.getUser(), message);
                return null;
            }
        };
        sendMessage.execute();
    }

    public static void sendActionToChannel(final Server server, final String channelName,
                                           final String action) {
        final AsyncTask<Void, Void, Void> sendAction = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final Channel channel = server.getUserChannelInterface().getChannel(channelName);
                channel.getWriter().sendAction(action);

                MessageSender.getSender(server.getTitle()).sendAction(channelName,
                        server.getUser(), action);
                return null;
            }
        };
        sendAction.execute();
    }

    public static void sendMessageToUser(final Server server, final String userNick,
                                         final String message) {
        final AsyncTask<Void, Void, Void> sendMessage = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final User user = server.getUserChannelInterface().getUser(userNick);
                user.getWriter().sendMessage(message);

                MessageSender.getSender(server.getTitle()).sendPrivateMessage(user, message);
                return null;
            }
        };
        sendMessage.execute();
    }

    public static void sendActionToUser(final Server server, final String userNick,
                                        final String action) {
        final AsyncTask<Void, Void, Void> sendAction = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getUserChannelInterface().getUser(userNick).getWriter().sendAction(action);

                MessageSender.getSender(server.getTitle()).sendAction(userNick, server.getUser(), action);
                return null;
            }
        };
        sendAction.execute();
    }

    public static void sendNickChange(final Server server, final String newNick) {
        final AsyncTask<Void, Void, Void> changeNick = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getWriter().changeNick(newNick);
                return null;
            }
        };
        changeNick.execute();
    }

    public static void sendPart(final Server server, final String channelName,
                                final Context applicationContext) {
        final Channel channel = server.getUserChannelInterface().getChannel(channelName);
        sendPart(channel, applicationContext);
    }

    private static void sendPart(final Channel channel, final Context applicationContext) {
        final AsyncTask<Void, Void, Void> sendPart = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                channel.getWriter().partChannel(Utils.getPartReason(applicationContext));
                return null;
            }
        };
        sendPart.execute();
    }

    public static void sendClosePrivateMessage(final Server server, final User user) {
        final AsyncTask<Void, Void, Void> sendPart = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                server.getUser().closePrivateMessage(user);
                return null;
            }
        };
        sendPart.execute();
    }

    public static void sendUnknownEvent(final Server server, final String event) {
        final AsyncTask<Void, Void, Void> unknownEvent = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                /**server.getConfiguration().getListenerManager()
                 .dispatchEvent(new UnknownEvent<>(server, event));*/
                return null;
            }
        };
        unknownEvent.execute();
    }

    public static void sendUserWhois(final Server server, final String nick) {
        final AsyncTask<Void, Void, Void> whois = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                //server.sendIRC().whois(nick);
                return null;
            }
        };
        whois.execute();
    }
}