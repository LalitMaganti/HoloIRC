/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.communication;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.ui.ServerListActivity;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.connection.ConnectionManager;
import com.fusionx.relay.event.channel.MentionEvent;
import com.squareup.otto.Subscribe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Pair;

import java.util.HashMap;

public class IRCService extends Service {

    public static final String MENTIONACTIVITY = "MENTIONACTIVITY";

    private final IRCBinder mBinder = new IRCBinder();

    private final Handler mAdapterHandler = new Handler(Looper.getMainLooper());

    private final AppPreferences mAppPreferences = new AppPreferences();

    private final HashMap<String, MentionHelper> mMentionHelperHashMap = new HashMap<>();

    private ConnectionManager mConnectionManager = null;

    private String mNoMention;

    public Server connectToServer(final ServerConfiguration configuration) {
        mConnectionManager = ConnectionManager.getConnectionManager(mAppPreferences);

        final Pair<Boolean, Server> serverPair = mConnectionManager.onConnectionRequested
                (configuration, mAdapterHandler);
        // The second element is the server itself
        final Server server = serverPair.second;

        // The first element is true if the server exists
        if (!serverPair.first) {
            final MentionHelper helper = new MentionHelper(configuration);
            mMentionHelperHashMap.put(configuration.getTitle(), helper);
            server.getServerEventBus().register(helper);
        }

        updateNotification();

        return server;
    }

    private void updateNotification() {
        final String text = String.format(getResources().getQuantityString(R.plurals
                .server_connection, mConnectionManager.getServerCount()),
                mConnectionManager.getServerCount());
        final Intent intent = new Intent(this, ServerListActivity.class);
        final Intent intent2 = new Intent(this, IRCService.class);
        intent2.putExtra("stop", true);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        final PendingIntent pIntent2 = PendingIntent.getService(this, 0, intent2, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name)).setContentText(text).setTicker(text)
                        // TODO - change to a proper icon
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);

        final Notification notification = builder.addAction(android.R.drawable
                .ic_menu_close_clear_cancel, getString(R.string.service_disconnect_all),
                pIntent2).build();

        // Just a random number
        // TODO - maybe static int this?
        startForeground(1337, notification);
    }

    public void onDisconnectAll() {
        // Needed due to the fact that the connection manager can be null if the service was
        // restarted or if the theme is being changed
        if (mConnectionManager != null) {
            synchronized (mBinder) {
                for (final String title : mMentionHelperHashMap.keySet()) {
                    final Server server = mConnectionManager.getServerIfExists(title);
                    server.getServerEventBus().unregister(mMentionHelperHashMap.get(title));
                }
                mMentionHelperHashMap.clear();

                mConnectionManager.onDisconnectAll();
            }
        }
        stopForeground(true);

        final NotificationManager mNotificationManager = (NotificationManager) getSystemService
                (Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public void onRemoveServer(final String serverName) {
        synchronized (mBinder) {
            final Server server = mConnectionManager.getServerIfExists(serverName);
            server.getServerEventBus().unregister(mMentionHelperHashMap.get(serverName));
            mMentionHelperHashMap.remove(serverName);

            if (mConnectionManager.onDisconnectionRequested(serverName)) {
                stopForeground(true);
            } else {
                updateNotification();
                final NotificationManager mNotificationManager = (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(345);
            }
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        // This check is needed because a null intent will be sent when the service is killed by
        // the system and is restarted
        if (intent != null) {
            if (intent.getBooleanExtra("stop", false)) {
                onDisconnectAll();
                return 0;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return true;
    }

    public Server getServerIfExists(final String title) {
        if (mConnectionManager != null) {
            return mConnectionManager.getServerIfExists(title);
        } else {
            return null;
        }
    }

    public void setNoMention(String noMention) {
        mNoMention = noMention;
    }

    public class MentionHelper {

        private final ServerConfiguration mConfiguration;

        private MentionHelper(final ServerConfiguration configuration) {
            mConfiguration = configuration;
        }

        // Subscribe events
        @Subscribe
        public void onMention(final MentionEvent event) {
            if (!mConfiguration.getTitle().equals(mNoMention)) {
                final NotificationManager mNotificationManager = (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
                final Intent intent = new Intent(IRCService.this, UIUtils.getIRCActivity(IRCService
                        .this));
                intent.putExtra("serverTitle", mConfiguration.getTitle());
                intent.putExtra("serverConfig", mConfiguration);
                intent.putExtra("mention", event.channelName);
                final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(IRCService.this);
                taskStackBuilder.addParentStack(UIUtils.getIRCActivity(IRCService.this));
                taskStackBuilder.addNextIntent(intent);
                final PendingIntent pIntent = taskStackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                final Notification notification = new NotificationCompat.Builder(IRCService.this)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.service_you_mentioned) + " " + event
                                .channelName)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true)
                        .setTicker(getString(R.string.service_you_mentioned) + " " + event
                                .channelName)
                        .setContentIntent(pIntent).build();
                mNotificationManager.notify(345, notification);
            }
        }
        // Subscribe events end
    }

    // Binder which returns this service
    public class IRCBinder extends Binder {

        public IRCService getService() {
            return IRCService.this;
        }
    }
}