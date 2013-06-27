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

package com.fusionx.lightirc.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.activity.ServerChannelActivity;
import com.fusionx.lightirc.irc.LightBotFactory;
import com.fusionx.lightirc.irc.LightManager;
import com.fusionx.lightirc.listeners.ServiceListener;
import com.fusionx.lightirc.misc.LightThread;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UserChannelDao;
import org.pircbotx.output.OutputChannel;

public class IRCService extends Service {
    // Binder which returns this service
    public class IRCBinder extends Binder {
        public IRCService getService() {
            return IRCService.this;
        }
    }

    private final LightManager manager = new LightManager();
    private final IRCBinder mBinder = new IRCBinder();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void connectToServer(final Configuration.Builder server) {
        // TODO - setup option for this
        server.setAutoNickChange(true);
        server.setBotFactory(new LightBotFactory(this));

        setupListeners(server);
        setupNotification();

        final Configuration d = server.buildConfiguration();

        final PircBotX bo = new PircBotX(d);

        final LightThread thread = new LightThread(bo);
        thread.start();
        manager.put(server.getTitle(), thread);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupNotification() {
        final Intent intent = new Intent(this, MainServerListActivity.class);
        final Intent intent2 = new Intent(this, IRCService.class);
        intent2.putExtra("stop", true);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        final PendingIntent pIntent2 = PendingIntent.getService(this, 0,
                intent2, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("LightIRC")
                .setContentText("At least one server is joined")
                // TODO - change to a proper icon
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        Notification notification = builder
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        "Disconnect all", pIntent2).build();

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, notification);
    }

    private void disconnectAll() {
        manager.disconnectAll();
        stopForeground(true);
        stopSelf();
        Intent intent = new Intent("serviceStopped");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void disconnectFromServer(String serverName) {
        DisconnectTask disconnectTask = new DisconnectTask();
        disconnectTask.execute(serverName);
    }

    private class DisconnectTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(final String... strings) {
            if(getBot(strings[0]).getStatus().equals("Connected")) {
                getBot(strings[0]).shutdown();
            } else {
                manager.get(strings[0]).interrupt();
            }
            return strings[0];
        }

        @Override
        protected void onPostExecute(String strings) {
            manager.remove(strings);
            if (manager.size() == 0) {
                stopForeground(true);
                stopSelf();
            }
        }
    }

    public PircBotX getBot(final String serverName) {
        if(manager.get(serverName) != null) {
            return manager.get(serverName).getBot();
        } else {
            return null;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra("stop", false)) {
            disconnectAll();
            return 0;
        } else {
            return START_STICKY;
        }
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return true;
    }

    public void partFromChannel(String serverName, String channelName) {
        final UserChannelDao d = getBot(serverName).getUserChannelDao();
        final Channel c = d.getChannel(channelName);
        OutputChannel f = c.send();
        f.part();
    }

    public void removePrivateMessage(String serverName, String nick) {
        final UserChannelDao d = getBot(serverName).getUserChannelDao();
        d.removePrivateMessage(nick);
        d.getUser(nick).setBuffer("");
    }

    private void setupListeners(Configuration.Builder bot) {
        final ServiceListener mServiceListener = new ServiceListener();
        mServiceListener.setService(this);

        bot.getListenerManager().addListener(mServiceListener);
    }

    public void mention(String serverName, String messageDest) {
        final Intent mIntent = new Intent(this, ServerChannelActivity.class);
        mIntent.putExtra("server", new Configuration
                .Builder(getBot(serverName).getConfiguration()));
        mIntent.putExtra("mention", messageDest);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                mIntent, 0);
        final Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("LightIRC")
                .setContentText("You have been mentioned")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(345, notification);
    }
}