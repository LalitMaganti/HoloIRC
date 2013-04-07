package com.fusionx.lightirc.listeners;

import java.util.ArrayList;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.pircbotx.hooks.events.UserListEvent;

import com.fusionx.lightirc.irc.LightPircBotX;

public class ChannelListener extends IRCListener {

	@Override
	public void onMessage(final MessageEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick() + ": "
				+ event.getMessage() + "\n";

		getService().callbackToChannelAndAppend(event.getChannel().getName(),
				newMessage, event.getBot().getTitle());
	}

	@Override
	public void onTopic(final TopicEvent<LightPircBotX> event) {
		if (event.isChanged()) {
			final String newMessage = "The topic for this channel has been changed to: "
					+ event.getTopic()
					+ " by "
					+ event.getChannel().getTopicSetter() + "\n";
			getService().callbackToChannelAndAppend(
					event.getChannel().getName(), newMessage,
					event.getBot().getTitle());
		} else {
			getService().mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					final String newMessage = "The topic is: "
							+ event.getTopic() + " as set forth by "
							+ event.getChannel().getTopicSetter() + "\n";
					getService().callbackToChannelAndAppend(
							event.getChannel().getName(), newMessage,
							event.getBot().getTitle());
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

				getService().callbackToChannelAndAppend(c.getName(),
						newMessage, event.getBot().getTitle());
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

					getService().callbackToChannelAndAppend(c.getName(),
							newMessage, event.getBot().getTitle());
				}
			}
		} else {
			for (final Channel c : event.getBot().getChannels()) {
				final String newMessage = "You (" + event.getOldNick()
						+ ") are now known as " + event.getNewNick() + "\n";

				getService().callbackToChannelAndAppend(c.getName(),
						newMessage, event.getBot().getTitle());
			}
		}
	}

	@Override
	public void onPart(final PartEvent<LightPircBotX> event) {
		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			final String newMessage = event.getUser().getNick()
					+ " parted from the room\n";

			getService().callbackToChannelAndAppend(
					event.getChannel().getName(), newMessage,
					event.getBot().getTitle());
		}
	}

	@Override
	public void onJoin(final JoinEvent<LightPircBotX> event) {
		final String newMessage = event.getUser().getNick()
				+ " entered the room\n";
		if (!event.getUser().getNick().equals(event.getBot().getNick())) {
			getService().callbackToChannelAndAppend(
					event.getChannel().getName(), newMessage,
					event.getBot().getTitle());
		} else {
			getService().getBot(event.getBot().getTitle()).getChannelBuffers()
					.put(event.getChannel().getName(), newMessage);

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
		getService().callbackToChannelAndAppend(event.getChannel().getName(),
				newMessage, event.getBot().getTitle());
	}

	@Override
	public void onUserList(final UserListEvent<LightPircBotX> event) {
		ArrayList<String> array = new ArrayList<String>();
		for(User user : event.getChannel().getOps()) {
			array.add("@" + user.getNick());
		}
		for(User user : event.getChannel().getHalfOps()) {
			array.add("half@" + user.getNick());
		}
		for(User user : event.getChannel().getVoices()) {
			array.add("+" + user.getNick());
		}
		for(User user : event.getChannel().getNormalUsers()) {
			array.add(user.getNick());
		}
		getService().getChannelCallback(event.getChannel().getName()).userListChanged(array.toArray(new String[0]));
	}
}