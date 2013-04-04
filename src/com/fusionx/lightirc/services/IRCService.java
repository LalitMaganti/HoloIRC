package com.fusionx.lightirc.services;

import java.util.HashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.callbacks.ServerCallbacks;
import com.fusionx.lightirc.listeners.ChannelListener;
import com.fusionx.lightirc.listeners.ServerListener;
import com.fusionx.lightirc.misc.LightPircBotX;

public class IRCService extends Service {
	private final IRCBinder mBinder = new IRCBinder();

	private final ChannelListener mChannelListener = new ChannelListener();
	final ServerListener mServerListener = new ServerListener();

	public final HashMap<String, LightPircBotX> mServerObjects = new HashMap<String, LightPircBotX>();

	public class IRCBinder extends Binder {
		public IRCService getService() {
			return IRCService.this;
		}
	}

	@Override
	public IBinder onBind(final Intent arg0) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(final Intent arg0) {
		mServerListener.mServerCallbacks = null;
		mChannelListener.mServerCallbacks = null;
		mChannelListener.mSendCallback = false;
		return true;
	}

	@Override
	public void onCreate() {
		final IntentFilter filter = new IntentFilter(
				"com.fusionx.lightirc.MESSAGE_TO_CHANNEL");
		registerReceiver(mScreenStateReceiver, filter);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mScreenStateReceiver);
	}

	public LightPircBotX getBot(final String serverName) {
		return mServerObjects.get(serverName);
	}

	public void connectToServer(String serverName) {
		final LightPircBotX mServerObject = getBot(serverName);
		if (!mServerObject.mIsStarted) {
			// TODO - setup option for this
			mServerObject.setAutoNickChange(true);

			mServerListener.mServerObjects = mServerObjects;
			mChannelListener.mServerObjects = mServerObjects;
			
			mServerObject.getListenerManager().addListener(mServerListener);
			mServerObject.getListenerManager().addListener(mChannelListener);

			mServerObject.mIsStarted = true;

			Thread runner = new Thread() {
				public void run() {
					try {
						mServerObject.connect(mServerObject.mURL);
						for (String channel : mServerObject.mAutoJoinChannels) {
							mServerObject.joinChannel(channel);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			runner.start();
		}
	}

	final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent.getAction().equals(
					"com.fusionx.lightirc.CHANNEL_MESSAGE_TO_PARSE")) {
				String channelName = intent.getStringExtra("channel");
				String message = intent.getStringExtra("message");
				String serverName = intent.getStringExtra("serverName");
				LightPircBotX bot = mServerObjects.get(serverName);

				bot.sendMessage(channelName, message);

				String bufferMessage = bot.getNick() + ": " + message + "\n";

				mChannelListener.callbackToChannelAndAppend(channelName,
						bufferMessage, serverName);
			}
		}
	};

	public void addToServers(String title, LightPircBotX bot) {
		mServerObjects.put(title, bot);
	}

	public void setChannelCallbacks(final ChannelCallbacks cb,
			final String channelName) {
		mChannelListener.mChannelCallbacks.put(channelName, cb);
	}

	public void setServerCallbacks(final ServerCallbacks cb) {
		mServerListener.mServerCallbacks = cb;
		mChannelListener.mSendCallback = true;
		mChannelListener.mServerCallbacks = cb;
	}
}