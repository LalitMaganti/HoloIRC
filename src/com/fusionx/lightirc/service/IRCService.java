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
import android.support.v4.app.TaskStackBuilder;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.IRCFragmentActivity;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.irc.LightBotFactory;
import com.fusionx.lightirc.irc.LightManager;
import com.fusionx.lightirc.irc.LightThread;
import com.fusionx.lightirc.listeners.ServiceListener;
import com.fusionx.lightirc.misc.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.managers.ListenerManager;

public class IRCService extends Service {
    // Binder which returns this service
    public class IRCBinder extends Binder {
        public IRCService getService() {
            return IRCService.this;
        }
    }

    @Getter(AccessLevel.PUBLIC)
    private LightManager threadManager = null;
    private final IRCBinder mBinder = new IRCBinder();
    @Setter(AccessLevel.PUBLIC)
    private String boundToServer = null;

    public void connectToServer(final Configuration.Builder server) {
        threadManager = new LightManager(getApplicationContext());

        final LightBotFactory factory = new LightBotFactory(getApplicationContext());
        server.setBotFactory(factory);

        setupListeners(server);
        setupNotification();

        @SuppressWarnings("unchecked")
        final Configuration<PircBotX> configuration = server.buildConfiguration();

        final PircBotX bot = new PircBotX(configuration);
        bot.setStatus(getString(R.string.status_connecting));

        final LightThread thread = new LightThread(bot);
        thread.start();
        threadManager.put(server.getTitle(), thread);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupNotification() {
        final Intent intent = new Intent(this, MainServerListActivity.class);
        final Intent intent2 = new Intent(this, IRCService.class);
        intent2.putExtra("stop", true);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final PendingIntent pIntent2 = PendingIntent.getService(this, 0, intent2, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_lightirc_running))
                .setTicker(getString(R.string.service_lightirc_running))
                        // TODO - change to a proper icon
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        final Notification notification = builder.addAction(android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.service_disconnect_all), pIntent2).build();

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, notification);
    }

    public void disconnectAll() {
        threadManager.disconnectAll();

        stopForeground(true);
    }

    public void disconnectFromServer(final String serverName) {
        final AsyncTask<String, Void, String> disconnectTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(final String... strings) {
                final String botName = strings[0];

                final ListenerManager<PircBotX> manager = getBot(botName).getConfiguration().getListenerManager();
                for(final Listener l : manager.getListeners()) {
                    manager.removeListener(l);
                }

                if (getBot(botName).getStatus().equals(getString(R.string.status_connected))) {
                    getBot(botName).setStatus(getString(R.string.status_disconnected));
                    getBot(botName).sendIRC().quitServer(Utils.getQuitReason(getApplicationContext()));
                    getBot(botName).shutdown();
                } else {
                    getBot(botName).setStatus(getString(R.string.status_disconnected));
                    getThreadManager().get(botName).interrupt();
                }
                return botName;
            }

            @Override
            protected void onPostExecute(final String botName) {
                threadManager.remove(botName);
                if (getThreadManager().keySet().isEmpty()) {
                    stopForeground(true);
                }
            }
        };
        disconnectTask.execute(serverName);
    }

    public PircBotX getBot(final String serverName) {
        if (threadManager != null && threadManager.get(serverName) != null) {
            return threadManager.get(serverName).getBot();
        } else {
            return null;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            boundToServer = intent.getStringExtra("setBound");
            if (intent.getBooleanExtra("stop", false)) {
                disconnectAll();
            }
            return 0;
        } else {
            return START_STICKY;
        }
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        boundToServer = null;

        return true;
    }

    private void setupListeners(final Configuration.Builder bot) {
        final ServiceListener mServiceListener = new ServiceListener(this);
        bot.getListenerManager().addListener(mServiceListener);
    }

    public void mention(final String serverName, final String messageDestination) {
        Notification notification;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_you_mentioned) + " " + messageDestination)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setTicker(getString(R.string.service_you_mentioned) + " " + messageDestination);

        if (!serverName.equals(boundToServer)) {
            final Intent mIntent = new Intent(this, IRCFragmentActivity.class);
            mIntent.putExtra("server", new Configuration.Builder<PircBotX>(getBot(serverName).getConfiguration()));
            mIntent.putExtra("mention", messageDestination);
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addParentStack(IRCFragmentActivity.class);
            taskStackBuilder.addNextIntent(mIntent);
            final PendingIntent pIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = builder.setContentIntent(pIntent).build();
        } else {
            notification = builder.build();
        }

        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(345, notification);
        mNotificationManager.cancel(345);
    }
}