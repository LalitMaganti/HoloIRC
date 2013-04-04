package com.fusionx.lightirc.listeners;

import java.util.HashMap;

import org.pircbotx.Channel;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.misc.LightPircBotX;

public class ChannelListener extends IRCListener {
	public final HashMap<String, ChannelCallbacks> mChannelCallbacks = new HashMap<String, ChannelCallbacks>();
	public boolean mSendCallback = true;

	@Override
	public void onMessage(final MessageEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick() + ": "
				+ event.getMessage() + "\n";

		callbackToChannelAndAppend(event.getChannel().getName(), newMessage,
				event.getBot().getTitle());
	}

	@Override
	public void onTopic(final TopicEvent<LightPircBotX> event) {
		if (event.isChanged()) {

			final String newMessage = "The topic for this channel has been changed to: "
					+ event.getTopic()
					+ " by "
					+ event.getChannel().getTopicSetter() + "\n";
			callbackToChannelAndAppend(event.getChannel().getName(),
					newMessage, event.getBot().getTitle());
		} else {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					final String newMessage = "The topic is: "
							+ event.getTopic() + " as set forth by "
							+ event.getChannel().getTopicSetter() + "\n";
					callbackToChannelAndAppend(event.getChannel().getName(),
							newMessage, event.getBot().getTitle());
				}
			}, 2000);
		}
	}

	@Override
	public void onQuit(final QuitEvent<LightPircBotX> event) {
		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			for (final Channel c : event.getBot().getChannels()) {
				if (c.getUsers().contains(event.getUser())) {
					final String newMessage = event.getUser().getNick()
							+ " quit the room\n";

					callbackToChannelAndAppend(c.getName(), newMessage, event
							.getBot().getTitle());
				}
			}

		}
	}

	@Override
	public void onNickChange(final NickChangeEvent<LightPircBotX> event) {
		if (!event.getOldNick().equals(event.getBot().getNick())) {
			for (final Channel c : event.getBot().getChannels()) {
				if (c.getUsers().contains(event.getUser())) {
					final String newMessage = event.getOldNick()
							+ " is now known as " + event.getNewNick() + "\n";

					callbackToChannelAndAppend(c.getName(), newMessage, event
							.getBot().getTitle());
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
	public void onPart(final PartEvent<LightPircBotX> event) {
		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			final String newMessage = event.getUser().getNick()
					+ " parted from the room\n";

			callbackToChannelAndAppend(event.getChannel().getName(),
					newMessage, event.getBot().getTitle());
		}
	}

	@Override
	public void onJoin(final JoinEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick()
				+ " entered the room\n";
		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			callbackToChannelAndAppend(event.getChannel().getName(),
					newMessage, event.getBot().getTitle());
		} else {
			mServerObjects.get(event.getBot().getTitle()).mChannelBuffers.put(
					event.getChannel().getName(), newMessage);

			tryPostServer(new Runnable() {
				@Override
				public void run() {
					mServerCallbacks.onNewChannelJoined(event.getChannel()
							.getName(), event.getUser().getNick(), newMessage);
				}
			});
		}
	}

	public void callbackToChannelAndAppend(final String channelName,
			final String message, final String serverName) {
		final HashMap<String, String> buffers = mServerObjects.get(serverName).mChannelBuffers;
		if (mChannelCallbacks.get(channelName) != null && mSendCallback) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mChannelCallbacks.get(channelName).onChannelWriteNeeded(
							message);
				}
			});
		}

		final String buffer = buffers.get(channelName) + message;
		buffers.put(channelName, buffer);
	}
}