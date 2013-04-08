package com.fusionx.lightirc.services;

import java.util.HashMap;

import org.pircbotx.Channel;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.callbacks.ServerCallback;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.irc.LightPircBotX;
import com.fusionx.lightirc.listeners.ChannelListener;
import com.fusionx.lightirc.listeners.ServerListener;
import com.fusionx.lightirc.parser.ChannelMessageParser;
import com.fusionx.lightirc.parser.ServerMessageParser;

public class IRCService extends Service {
	private static int noOfConnections;

	private final HashMap<String, LightPircBotX> manager = new HashMap<String, LightPircBotX>();

	private final IRCBinder mBinder = new IRCBinder();

	private ServerCallback mServerCallback = null;
	private final HashMap<String, ChannelCallbacks> mChannelCallbacks = new HashMap<String, ChannelCallbacks>();

	private final ChannelMessageParser mChannelMessageReceiver = new ChannelMessageParser();
	private final ServerMessageParser mServerMessageReciever = new ServerMessageParser();

	// Handler is common across listeners and parser
	public final Handler mHandler = new Handler();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getBooleanExtra("stop", false)) {
			disconnectAll();
			return 0;
		} else {
			return START_STICKY;
		}
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		mServerCallback = null;
		return true;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mChannelMessageReceiver);
		unregisterReceiver(mServerMessageReciever);
	}

	@SuppressLint("NewApi")
	public void connectToServer(final LightPircBotX server) {
		final LightPircBotX bot = server;
		if (!bot.isStarted()) {
			// TODO - setup option for this
			bot.setAutoNickChange(true);

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

			new Thread() {
				@Override
				public void run() {
					bot.connectAndAutoJoin();
				}
			}.start();

			noOfConnections += 1;

			manager.put(server.getTitle(), bot);
		}
	}

	private void setupListeners(LightPircBotX bot) {
		mChannelMessageReceiver.setService(this);
		mServerMessageReciever.setService(this);

		final IntentFilter filter = new IntentFilter(
				"com.fusionx.lightirc.CHANNEL_MESSAGE_TO_PARSE");
		registerReceiver(mChannelMessageReceiver, filter);

		final IntentFilter serverFilter = new IntentFilter(
				"com.fusionx.lightirc.SERVER_MESSAGE_TO_PARSE");
		registerReceiver(mServerMessageReciever, serverFilter);

		final ChannelListener mChannelListener = new ChannelListener();
		final ServerListener mServerListener = new ServerListener();

		mChannelListener.setService(this);
		mServerListener.setService(this);

		bot.getListenerManager().addListener(mServerListener);
		bot.getListenerManager().addListener(mChannelListener);
	}

	public void callbackToChannelAndAppend(final Channel channel,
			final String message) {

		if (mChannelCallbacks.get(channel.getName()) != null
				&& mServerCallback != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mChannelCallbacks.get(channel.getName()).writeToTextView(
							message);
				}
			});
		}

		((LightChannel) channel).appendToBuffer(message);
	}
	
	public void callbackToChannelAndAppend(final String channel,
			final String message, String botTitle) {
		callbackToChannelAndAppend(getBot(botTitle).getChannel(channel), message);
	}

	public void callbackToServerAndAppend(final LightPircBotX bot,
			final String message) {
		if (getServerCallback() != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					getServerCallback().writeToTextView(message);
				}
			});
		}

		bot.appendToBuffer(message);
	}
	
	public void callbackToServerAndAppend(final String bot,
			final String message) {
		callbackToServerAndAppend(getBot(bot), message);
	}

	public void partFromChannel(String serverName, String channelName) {
		getBot(serverName).partChannel(channelName);
		mChannelCallbacks.remove(channelName);
	}

	public void disconnectFromServer(String serverName) {
		getBot(serverName).disconnect();
		manager.remove(serverName);
		noOfConnections -= 1;
		if (noOfConnections == 0) {
			stopForeground(true);
			stopSelf();
		}
	}

	private void disconnectAll() {
		for (LightPircBotX bot : manager.values()) {
			bot.disconnect();
		}
		noOfConnections = 0;
		stopForeground(true);
		stopSelf();
	}

	// Getters and setters

	// public only for parsers - should not be used elsewhere
	public LightPircBotX getBot(final String serverName) {
		return manager.get(serverName);
	}

	public ChannelCallbacks getChannelCallback(String channelName) {
		return mChannelCallbacks.get(channelName);
	}

	public void setChannelCallbacks(final ChannelCallbacks cb,
			final String channelName, final String serverName) {
		mChannelCallbacks.put(channelName, cb);
		cb.userListChanged(((LightChannel) getBot(serverName).getChannel(
				channelName)).getUserNicks());
	}

	public ServerCallback getServerCallback() {
		return mServerCallback;
	}

	public void setServerCallback(ServerCallback serverCallback) {
		mServerCallback = serverCallback;
	}

	public int getNumberOfServers() {
		return manager.size();
	}

	// Binder which returns this service
	public class IRCBinder extends Binder {
		public IRCService getService() {
			return IRCService.this;
		}
	}
}
