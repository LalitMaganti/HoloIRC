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
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;

import java.util.ArrayList;
import java.util.Collections;

public class ServiceListener extends GenericListener {

    @Override
    public void onEvent(final Event<LightBot> event) throws Exception {
        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            event.getBot().appendToBuffer(Utils.getOutputForEvent(event));
        }
        super.onEvent(event);
    }

    @Override
    public void onAction(final ActionEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void userJoin(final JoinEvent<LightBot> event) {
        // Invalid
    }

    @Override
    public void otherUserJoin(final JoinEvent<LightBot> event) {
        ArrayList<String> set = ((LightChannel) event.getChannel()).getUserList();
        set.add(event.getUser().getNick());
        Collections.sort(set);
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void part(final PartEvent<LightBot> event) {
        ArrayList<String> set = ((LightChannel) event.getBot().getUserChannelDao().getChannel(event.getChannel().getName())).getUserList();
        event.getBot().getUserChannelDao().getChannel(event.getChannel().getName())
                .appendToBuffer(Utils.getOutputForEvent(event));
        set.remove(event.getUser().getNick());
    }

    @Override
    public void onMessage(final MessageEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<LightBot> event) {
        ArrayList<String> set = ((LightChannel) event.getBot().getUserChannelDao().getChannel(event.getChannel().getName())).getUserList();
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
        set.remove(event.getUser().getNick());
    }

    @Override
    public void onNickChange(final NickChangeEvent<LightBot> event) {
        for (final Channel c : event.getBot().getUserBot().getChannels()) {
            ArrayList<String> set = ((LightChannel) c).getUserList();
            for (String string : set) {
                if (string.contains(event.getOldNick())) {
                    c.appendToBuffer(Utils.getOutputForEvent(event));
                    set.set(set.indexOf(string), event.getNewNick());
                    Collections.sort(set);
                }
            }
        }
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<LightBot> event) {
        ArrayList<String> set = ((LightChannel) event.getChannel()).getUserList();
        for (String string : set) {
            if (string.contains(event.getOldNick())) {
                event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
                set.set(set.indexOf(string), event.getNewNick());
                Collections.sort(set);
            }
        }
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }
}