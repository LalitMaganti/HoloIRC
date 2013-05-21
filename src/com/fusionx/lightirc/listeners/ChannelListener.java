package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.misc.EventOutput;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;

import java.util.ArrayList;
import java.util.Set;

public class ChannelListener extends IRCListener {
    @Override
    public void onAction(final ActionEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(EventOutput.getOutputForEvent(event));
    }

    @Override
    public void onJoin(final JoinEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(EventOutput.getOutputForEvent(event));
    }

    @Override
    public void onMessage(final MessageEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(EventOutput.getOutputForEvent(event));
    }

    @Override
    public void onNickChange(final NickChangeEvent<LightBot> event) {
        for (final Channel c : event.getBot().getUserBot().getChannels()) {
            ArrayList<String> set = ((LightChannel) c).getCleanUserNicks();
            if (set.contains(event.getOldNick()) || set.contains(event.getNewNick())) {
                ((LightChannel) c).appendToBuffer(EventOutput.getOutputForEvent(event));
            }
        }
    }

    @Override
    public void onPart(final PartEvent<LightBot> event) {
        if (!event.getUser().getNick().equals(event.getBot().getNick())) {
            ((LightChannel) event.getChannel()).appendToBuffer(
                    EventOutput.getOutputForEvent(event));
        }
    }

    @Override
    public void onQuit(final QuitEvent<LightBot> event) {
        for (final Channel c : event.getUser().getChannels()) {
            if (event.getBot().getUserBot().getChannels().contains(c)) {
                ((LightChannel) c).appendToBuffer(EventOutput.getOutputForEvent(event));
            }
        }
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(EventOutput.getOutputForEvent(event));
    }
}
