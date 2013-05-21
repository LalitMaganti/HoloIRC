package com.fusionx.lightirc.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightBotFactory;
import com.fusionx.lightirc.irc.LightBuilder;
import com.fusionx.lightirc.irc.LightPircBotXManager;
import com.fusionx.lightirc.listeners.ChannelListener;
import com.fusionx.lightirc.listeners.ServerListener;
import com.fusionx.lightirc.parser.ChannelMessageParser;
import com.fusionx.lightirc.parser.ServerMessageParser;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.UserChannelDao;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.output.OutputChannel;

import java.io.IOException;

public class IRCService extends Service {
    // Binder which returns this service
    public class IRCBinder extends Binder {
        public IRCService getService() {
            return IRCService.this;
        }
    }

    private final LightPircBotXManager manager = new LightPircBotXManager();
    private final IRCBinder mBinder = new IRCBinder();
    private final ChannelMessageParser mChannelMessageReceiver = new ChannelMessageParser();
    private final ServerMessageParser mServerMessageReceiver = new ServerMessageParser();

    private void appendToServer(final LightBot bot,
                                final String message) {
        bot.appendToBuffer(message);
    }

    public void callbackToServerAndAppend(final String bot, final String message) {
        appendToServer(getBot(bot), message);
    }

    @SuppressLint("NewApi")
    public void connectToServer(final LightBuilder server) {
        final LightBuilder bot = server;
        if (manager.get(server) == null || !manager.get(server).isConnected()) {
            // TODO - setup option for this
            bot.setAutoNickChange(true);
            bot.setBotFactory(new LightBotFactory());

            setupListeners(bot);

            final Intent intent = new Intent(this, MainServerListActivity.class);
            final Intent intent2 = new Intent(this, IRCService.class);
            intent2.putExtra("stop", true);
            final PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                    intent, 0);
            final PendingIntent pIntent2 = PendingIntent.getService(this, 0,
                    intent2, 0);

            final Notification noti = new Notification.Builder(this)
                    .setContentTitle("LightIRC")
                    .setContentText("At least one server is joined")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                            "Disconnect all", pIntent2).build();
            // Just a random number
            // TODO - maybe static int this?
            startForeground(1337, noti);

            Configuration d = bot.buildConfiguration();

            final LightBot bo = new LightBot(d, bot.getTitle());

            new Thread() {
                @Override
                public void run() {
                    try {
                        bo.connect();
                    } catch (NickAlreadyInUseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IrcException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            manager.put(server.getTitle(), bo);
        }
    }

    private void disconnectAll() {
        manager.disconnectAll();
        stopForeground(true);
        stopSelf();
    }

    public void disconnectFromServer(String serverName) {
        getBot(serverName).shutdown();
        manager.remove(serverName);
        if (manager.size() == 0) {
            stopForeground(true);
            stopSelf();
        }
    }

    // public only for parsers - should not be used elsewhere
    public LightBot getBot(final String serverName) {
        return manager.get(serverName);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mChannelMessageReceiver);
        unregisterReceiver(mServerMessageReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //if (intent.getBooleanExtra("stop", false)) {
        //    disconnectAll();
        //    return 0;
        //} else {
        return START_STICKY;
        //}
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return true;
    }

    public void partFromChannel(String serverName, String channelName) {
        UserChannelDao d = getBot(serverName).getUserChannelDao();
        Channel c = d.getChannel(channelName);
        OutputChannel f = c.send();
        f.part();
    }

    private void setupListeners(LightBuilder bot) {
        mChannelMessageReceiver.setService(this);
        mServerMessageReceiver.setService(this);

        final IntentFilter filter = new IntentFilter(
                "com.fusionx.lightirc.CHANNEL_MESSAGE_TO_PARSE");
        registerReceiver(mChannelMessageReceiver, filter);

        final IntentFilter serverFilter = new IntentFilter(
                "com.fusionx.lightirc.SERVER_MESSAGE_TO_PARSE");
        registerReceiver(mServerMessageReceiver, serverFilter);

        final ChannelListener mChannelListener = new ChannelListener();
        final ServerListener mServerListener = new ServerListener();

        mChannelListener.setService(this);
        mServerListener.setService(this);

        bot.getListenerManager().addListener(mServerListener);
        bot.getListenerManager().addListener(mChannelListener);
    }
}
