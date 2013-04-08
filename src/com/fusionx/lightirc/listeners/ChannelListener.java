package com.fusionx.lightirc.listeners;

import org.pircbotx.Channel;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.pircbotx.hooks.events.UserListEvent;

import com.fusionx.lightirc.callbacks.ChannelCallbacks;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.irc.LightPircBotX;

public class ChannelListener extends IRCListener {

	@Override
	public void onMessage(final MessageEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick() + ": "
				+ event.getMessage() + "\n";

		getService().callbackToChannelAndAppend(event.getChannel(), newMessage);
	}

	@Override
	public void onTopic(final TopicEvent<LightPircBotX> event) {
		if (event.isChanged()) {
			final String newMessage = "The topic for this channel has been changed to: "
					+ event.getTopic()
					+ " by "
					+ event.getChannel().getTopicSetter() + "\n";
			getService().callbackToChannelAndAppend(event.getChannel(),
					newMessage);
		} else {
			getService().mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					final String newMessage = "The topic is: "
							+ event.getTopic() + " as set forth by "
							+ event.getChannel().getTopicSetter() + "\n";
					getService().callbackToChannelAndAppend(event.getChannel(),
							newMessage);
				}
			}, 1500);
		}
	}

	@Override
	public void onQuit(final QuitEvent<LightPircBotX> event) {
		for (final Channel c : event.getUser().getChannels()) {
			if (event.getBot().getChannels().contains(c)) {
				final String newMessage = event.getUser().getNick()
						+ " quit the room\n";

				getService().callbackToChannelAndAppend(c, newMessage);

				ChannelCallbacks cc = getService().getChannelCallback(
						c.getName());
				if (cc != null) {
					cc.userListChanged(((LightChannel) c).getUserNicks());
				}
			}
		}
	}

	@Override
	public void onNickChange(final NickChangeEvent<LightPircBotX> event) {
		String newMessage;
		if (!event.getUser().equals(event.getBot().getUserBot())) {
			newMessage = event.getOldNick() + " is now known as "
					+ event.getNewNick() + "\n";
		} else {
			newMessage = "You (" + event.getOldNick() + ") are now known as "
					+ event.getNewNick() + "\n";
		}
		for (final Channel c : event.getUser().getChannels()) {
			if (event.getBot().getChannels().contains(c)) {
				getService().callbackToChannelAndAppend(c, newMessage);
				ChannelCallbacks cc = getService().getChannelCallback(
						c.getName());
				if (cc != null) {
					cc.userListChanged(((LightChannel) c).getUserNicks());
				}
			}
		}
	}

	@Override
	public void onPart(final PartEvent<LightPircBotX> event) {
		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			final String newMessage = event.getUser().getNick()
					+ " parted from the room\n";

			getService().callbackToChannelAndAppend(event.getChannel(),
					newMessage);

			ChannelCallbacks cc = getService().getChannelCallback(
					event.getChannel().getName());
			if (cc != null) {
				cc.userListChanged(((LightChannel) event.getChannel())
						.getUserNicks());
			}
		}
	}

	@Override
	public void onJoin(final JoinEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick()
				+ " entered the room\n";

		LightChannel lightchannel = (LightChannel) (event.getChannel());

		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			getService().callbackToChannelAndAppend(lightchannel, newMessage);

			ChannelCallbacks cc = getService().getChannelCallback(
					event.getChannel().getName());
			if (cc != null) {
				cc.userListChanged(((LightChannel) event.getChannel())
						.getUserNicks());
			}
		} else {
			lightchannel.setBuffer(newMessage);

			getService().mHandler.post(new Runnable() {
				@Override
				public void run() {
					getService().getServerCallback().onNewChannelJoined(
							event.getChannel().getName(),
							event.getUser().getNick(), newMessage);
				}
			});
		}
	}

	@Override
	public void onAction(final ActionEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick() + " "
				+ event.getAction();
		getService().callbackToChannelAndAppend(event.getChannel(), newMessage);
	}

	@Override
	public void onUserList(final UserListEvent<LightPircBotX> event) {
		LightChannel channel = (LightChannel) event.getChannel();

		ChannelCallbacks cc = getService().getChannelCallback(
				event.getChannel().getName());
		if (cc != null) {
			cc.userListChanged(channel.getUserNicks());
		}
	}
}
