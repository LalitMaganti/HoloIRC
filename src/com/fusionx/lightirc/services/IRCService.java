package com.fusionx.lightirc.services;

import java.util.HashMap;

import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NoticeEvent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;

import com.fusionx.lightirc.misc.IRCBinder;
import com.fusionx.lightirc.misc.LightPircBotX;

public class IRCService extends Service {
	private final IRCBinder mBinder;
	public final HashMap<String, LightPircBotX> mServerObjects = new HashMap<String, LightPircBotX>();

	public IRCService() {
		mBinder = new IRCBinder(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// mHandler = new Handler();
		return mBinder;
	}

	@Override
	public void onRebind(Intent arg0) {
		// mHandler = new Handler();
	}

	@Override
	public boolean onUnbind(Intent arg0) {
		// unregisterReceiverCallbacks();
		// mHandler = null;
		return true;
	}

	@Override
	public void onCreate() {
		IntentFilter filter = new IntentFilter(
				"com.fusionx.lightirc.MESSAGE_TO_CHANNEL");
		registerReceiver(mScreenStateReceiver, filter);
	}

	@Override
	public void onDestroy() {
	}
	
	public LightPircBotX getBot(String serverName) {
		return mServerObjects.get(serverName);
	}
	
	AsyncTask<LightPircBotX, Void, Void> mBackgroundConnector = new AsyncTask<LightPircBotX, Void, Void>() {
		@Override
		protected Void doInBackground(LightPircBotX... serverDetails) {
			try {
				serverDetails[0].connect(serverDetails[0].mURL);
				serverDetails[0].joinChannel(serverDetails[0].mAutoJoinChannels[0]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
	};

	public void connectToServer(String serverName) {
		LightPircBotX mServerObject = getBot(serverName);
		if(!mServerObject.mIsStarted) {
			// TODO - setup option for this
			mServerObject.setAutoNickChange(true);

			final ServerListener listener = new ServerListener();
			mServerObject.getListenerManager().addListener(listener);

			final ChannelListener listener2 = new ChannelListener();
			mServerObject.getListenerManager().addListener(listener2);

			mServerObject.setName(mServerObject.mNick);
			mServerObject.mIsStarted = true;

			mBackgroundConnector.execute(mServerObject, null, null);
		}
	}

	BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					"com.fusionx.lightirc.MESSAGE_TO_CHANNEL")) {
				String channelName = intent.getStringExtra("channel");
				String message = intent.getStringExtra("message");
				String serverName = intent.getStringExtra("serverName");
				mServerObjects.get(serverName).sendMessage(mServerObjects.get(serverName).getChannel(channelName), message);
			}
		}
	};

	public class ServerListener extends ListenerAdapter<LightPircBotX> implements
			Listener<LightPircBotX> {
		@Override
		public void onJoin(final JoinEvent<LightPircBotX> event) throws Exception {
			if (event.getUser().getNick().equals(event.getBot().getNick())) {
				Intent intent = new Intent();
				intent.setAction("com.fusionx.lightirc.JOIN_NEW_CHANNEL");
				intent.putExtra("channel", event.getChannel().getName());
				intent.putExtra("nick", event.getUser().getNick());
				sendBroadcast(intent);
				
				String welcomeMessage = event.getUser().getNick() + " entered the room\n";
				
				mServerObjects.get(event.getBot().getTitle()).mChannelBuffers.put(event.getChannel().getName(), welcomeMessage);
			}
		}

		@Override
		public void onNotice(final NoticeEvent<LightPircBotX> event)
				throws Exception {
			Intent intent = new Intent();
			intent.setAction("com.fusionx.lightirc.NOTICE_FROM_SERVER");
			intent.putExtra("stringToWrite", event.getMessage());
			sendBroadcast(intent);
			mServerObjects.get(event.getBot().getTitle()).mServerBuffer += event.getMessage() + "\n";
		}

		@Override
		public void onMotd(final MotdEvent<LightPircBotX> event) throws Exception {
			Intent intent = new Intent();
			intent.setAction("com.fusionx.lightirc.MOTD_FROM_SERVER");
			intent.putExtra("stringToWrite", event.getMotd());
			sendBroadcast(intent);
			mServerObjects.get(event.getBot().getTitle()).mServerBuffer += event.getMotd() + "\n";
		}
	}

	public class ChannelListener extends ListenerAdapter<LightPircBotX> implements
			Listener<LightPircBotX> {
		@Override
		public void onMessage(MessageEvent<LightPircBotX> event) throws Exception {
			String messageToWrite = event.getUser().getNick() + ": "
					+ event.getMessage();
			Intent intent = new Intent();
			intent.setAction("com.fusionx.lightirc.MESSAGE_FROM_CHANNEL");
			intent.putExtra("channel", event.getChannel().getName());
			intent.putExtra("message", messageToWrite);
			sendBroadcast(intent);
			String buffer = mServerObjects.get(event.getBot().getTitle()).mChannelBuffers.get(event.getChannel().getName());
			buffer += messageToWrite + "\n";
			mServerObjects.get(event.getBot().getTitle()).mChannelBuffers.put(event.getChannel().getName(), buffer);
		}
	}
}
