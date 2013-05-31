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

package com.fusionx.lightirc.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightBuilder;
import com.fusionx.lightirc.irc.LightManager;
import com.fusionx.lightirc.listeners.ServiceListener;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.UserChannelDao;
import org.pircbotx.exception.IrcException;
import org.pircbotx.output.OutputChannel;

import java.io.IOException;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;

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
    public void connectToServer(final LightBuilder server) {
        // TODO - setup option for this
        server.setAutoNickChange(true);

        setupListeners(server);
        setupNotification();

        Configuration d = server.buildConfiguration();

        final LightBot bo = new LightBot(d);

        new Thread() {
            @Override
            public void run() {
                try {
                    bo.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IrcException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        manager.put(server.getTitle(), bo);
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
        int currentAPIVersion = Build.VERSION.SDK_INT;
        Notification n;
        final Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("LightIRC")
                .setContentText("At least one server is joined")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);
        if (currentAPIVersion >= JELLY_BEAN) {
            n = builder.addAction(android.R.drawable.ic_menu_close_clear_cancel,
                    "Disconnect all", pIntent2).build();
        } else {
            //noinspection deprecation
            n = builder.getNotification();
        }

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, n);
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

    public LightBot getBot(final String serverName) {
        return manager.get(serverName);
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
        UserChannelDao d = getBot(serverName).getUserChannelDao();
        Channel c = d.getChannel(channelName);
        OutputChannel f = c.send();
        f.part();
    }

    private void setupListeners(LightBuilder bot) {
        final ServiceListener mChannelListener = new ServiceListener();

        bot.getListenerManager().addListener(mChannelListener);
    }
}
