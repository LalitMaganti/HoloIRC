package com.fusionx.lightirc.services;

import java.util.HashMap;

import org.pircbotx.Channel;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.MotdEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.callbacks.ServerCallbacks;
import com.fusionx.lightirc.misc.LightPircBotX;

public class IRCService extends Service {
	private final IRCBinder mBinder = new IRCBinder();
	public final HashMap<String, LightPircBotX> mServerObjects = new HashMap<String, LightPircBotX>();

	private final Handler mHandler = new Handler();
	public final HashMap<String, ChannelCallbacks> mChannelCallbacks = new HashMap<String, ChannelCallbacks>();
	private ServerCallbacks mServerCallbacks = null;

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
		mServerCallbacks = null;
		mChannelCallbacks.remove(arg0.getStringExtra("channelName"));
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

	private void callbackToServerAndAppend(final String message,
			final String serverName) {
		mServerObjects.get(serverName).mServerBuffer += message;

		tryPostServer(new Runnable() {
			public void run() {
				mServerCallbacks.onServerWriteNeeded(message);
			}
		});
	}

	AsyncTask<LightPircBotX, Void, Void> mBackgroundConnector = new AsyncTask<LightPircBotX, Void, Void>() {
		@Override
		protected Void doInBackground(LightPircBotX... serverDetails) {
			try {
				serverDetails[0].connect(serverDetails[0].mURL);
				for (String s : serverDetails[0].mAutoJoinChannels) {
					serverDetails[0].joinChannel(s);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
	};

	public void connectToServer(String serverName) {
		LightPircBotX mServerObject = getBot(serverName);
		if (!mServerObject.mIsStarted) {
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

	private void callbackToChannelAndAppend(final String channelName,
			final String message, final String serverName) {
		if (mChannelCallbacks.containsKey(channelName)) {
			mHandler.post(new Runnable() {
				public void run() {
					mChannelCallbacks.get(channelName).onChannelWriteNeeded(
							message);
				}
			});
		}

		final HashMap<String, String> buffers = mServerObjects.get(serverName).mChannelBuffers;
		final String buffer = buffers.get(channelName) + message;
		buffers.put(channelName, buffer);
	}

	final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent.getAction().equals(
					"com.fusionx.lightirc.MESSAGE_TO_CHANNEL")) {
				String channelName = intent.getStringExtra("channel");
				String message = intent.getStringExtra("message");
				String serverName = intent.getStringExtra("serverName");
				LightPircBotX bot = mServerObjects.get(serverName);

				bot.sendMessage(channelName, message);

				String bufferMessage = bot.getNick() + ": " + message + "\n";

				callbackToChannelAndAppend(channelName, bufferMessage,
						serverName);
			}
		}
	};

	public void setChannelCallbacks(final ChannelCallbacks cb,
			final String channelName) {
		mChannelCallbacks.put(channelName, cb);
	}

	public void setServerCallbacks(final ServerCallbacks cb) {
		mServerCallbacks = cb;
	}

	private void tryPostServer(final Runnable run) {
		if (mServerCallbacks != null) {
			mHandler.post(run);
		}
	}

	public class ServerListener extends ListenerAdapter<LightPircBotX>
			implements Listener<LightPircBotX> {
		@Override
		public void onJoin(final JoinEvent<LightPircBotX> event)
				throws Exception {
			if (event.getUser().getNick().equals(event.getBot().getNick())) {
				String buffer = event.getUser().getNick()
						+ " entered the room\n";
				if (!event.getChannel().getTopic().isEmpty()) {
					buffer += "The topic is: " + event.getChannel().getTopic()
							+ " as set forth by "
							+ event.getChannel().getTopicSetter() + "\n";
				} else {
					buffer += "There is no topic for this channel :(\n";
				}

				final String finalBuffer = buffer;

				mServerObjects.get(event.getBot().getTitle()).mChannelBuffers
						.put(event.getChannel().getName(), buffer);

				tryPostServer(new Runnable() {
					public void run() {
						mServerCallbacks.onNewChannelJoined(event.getChannel()
								.getName(), event.getUser().getNick(),
								finalBuffer);
					}
				});
			}
		}

		@Override
		public void onNotice(final NoticeEvent<LightPircBotX> event)
				throws Exception {
			callbackToServerAndAppend(event.getMessage() + "\n", event.getBot()
					.getTitle());

		}

		@Override
		public void onMotd(final MotdEvent<LightPircBotX> event)
				throws Exception {
			callbackToServerAndAppend(event.getMotd() + "\n", event.getBot()
					.getTitle());
		}
	}

	public class ChannelListener extends ListenerAdapter<LightPircBotX>
			implements Listener<LightPircBotX> {
		@Override
		public void onMessage(final MessageEvent<LightPircBotX> event)
				throws Exception {
			final String newMessage = event.getUser().getNick() + ": "
					+ event.getMessage() + "\n";

			callbackToChannelAndAppend(event.getChannel().getName(),
					newMessage, event.getBot().getTitle());
		}

		@Override
		public void onQuit(final QuitEvent<LightPircBotX> event)
				throws Exception {
			if (!event.getUser().getNick().equals(event.getBot().getNick())) {
				for (final Channel c : event.getBot().getChannels()) {
					if (c.getUsers().contains(event.getUser())) {
						final String newMessage = event.getUser().getNick()
								+ " quit the room\n";

						callbackToChannelAndAppend(c.getName(), newMessage,
								event.getBot().getTitle());
					}
				}

			}
		}

		@Override
		public void onNickChange(final NickChangeEvent<LightPircBotX> event)
				throws Exception {
			if (!event.getOldNick().equals(event.getBot().getNick())) {
				for (final Channel c : event.getBot().getChannels()) {
					if (c.getUsers().contains(event.getUser())) {
						final String newMessage = event.getOldNick()
								+ " is now known as " + event.getNewNick()
								+ "\n";

						callbackToChannelAndAppend(c.getName(), newMessage,
								event.getBot().getTitle());
					}
				}
			} else {
				for (final Channel c : event.getBot().getChannels()) {
					final String newMessage = "You (" + event.getOldNick()
							+ ") are now known as " + event.getNewNick() + "\n";

					callbackToChannelAndAppend(c.getName(), newMessage, event
							.getBot().getTitle());
				}
			}
		}

		@Override
		public void onPart(final PartEvent<LightPircBotX> event)
				throws Exception {
			if (!event.getUser().getNick().equals(event.getBot().getNick())) {
				final String newMessage = event.getUser().getNick()
						+ " parted from the room\n";

				callbackToChannelAndAppend(event.getChannel().getName(),
						newMessage, event.getBot().getTitle());
			}
		}

		@Override
		public void onJoin(final JoinEvent<LightPircBotX> event)
				throws Exception {
			if (!event.getUser().getNick().equals(event.getBot().getNick())) {
				final String newMessage = event.getUser().getNick()
						+ " entered the room\n";

				callbackToChannelAndAppend(event.getChannel().getName(),
						newMessage, event.getBot().getTitle());
			}
		}
	}
}