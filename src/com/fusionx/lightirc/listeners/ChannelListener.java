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

package com.fusionx.lightirc.listeners;

import com.fusionx.lightirc.irc.LightBot;
import com.fusionx.lightirc.irc.LightChannel;
import com.fusionx.lightirc.misc.Utils;
import org.pircbotx.Channel;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.ArrayList;

public class ChannelListener extends ListenerAdapter<LightBot> implements Listener<LightBot> {
    @Override
    public void onAction(final ActionEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void onJoin(final JoinEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void onMessage(final MessageEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void onNickChange(final NickChangeEvent<LightBot> event) {
        for (final Channel c : event.getBot().getUserBot().getChannels()) {
            ArrayList<String> set = ((LightChannel) c).getCleanUserNicks();
            if (set.contains(event.getOldNick()) || set.contains(event.getNewNick())) {
                ((LightChannel) c).appendToBuffer(Utils.getOutputForEvent(event));
            }
        }
    }

    @Override
    public void onPart(final PartEvent<LightBot> event) {
        if (!event.getUser().getNick().equals(event.getBot().getNick())) {
            ((LightChannel) event.getBot().getUserChannelDao().getChannel(event.getChannel().getName()))
                    .appendToBuffer(Utils.getOutputForEvent(event));
        }
    }

    @Override
    public void onQuit(final QuitEvent<LightBot> event) {
        for (final Channel c : event.getUser().getChannels()) {
            if (event.getBot().getUserBot().getChannels().contains(c)) {
                ((LightChannel) c).appendToBuffer(Utils.getOutputForEvent(event));
            }
        }
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        ((LightChannel) event.getChannel()).appendToBuffer(Utils.getOutputForEvent(event));
    }
}
