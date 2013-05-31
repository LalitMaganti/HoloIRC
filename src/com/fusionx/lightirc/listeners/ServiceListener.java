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
import com.fusionx.lightirc.misc.UserComparator;
import com.fusionx.lightirc.misc.Utils;
import org.pircbotx.Channel;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.PartEvent;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

import java.util.ArrayList;
import java.util.Collections;

public class ServiceListener extends GenericListener {

    @Override
    public void onEvent(final Event<LightBot> event) throws Exception {
        super.onEvent(event);

        if (event instanceof MotdEvent || event instanceof NoticeEvent) {
            event.getBot().appendToBuffer(Utils.getOutputForEvent(event));
        }
    }

    @Override
    public void onAction(final ActionEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void userJoin(final JoinEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void otherUserJoin(final JoinEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.add(event.getUser().getPrettyNick());
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void part(final PartEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.remove(event.getUser().getPrettyNick());
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void onMessage(final MessageEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }

    @Override
    public void onQuitPerChannel(final QuitEventPerChannel<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));

        final ArrayList<String> set = event.getChannel().getUserList();
        set.remove(event.getUser().getPrettyNick());
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void onNickChange(final NickChangeEvent<LightBot> event) {
        for (final Channel c : event.getBot().getUserBot().getChannels()) {
            final ArrayList<String> set = c.getUserList();
            final String oldFormattedNick = event.getOldNick();
            final String newFormattedNick = event.getNewNick();

            c.appendToBuffer(Utils.getOutputForEvent(event));
            set.set(set.indexOf(oldFormattedNick), newFormattedNick);
            Collections.sort(set, new UserComparator());
        }
    }

    @Override
    public void onNickChangePerChannel(final NickChangeEventPerChannel<LightBot> event) {
        final ArrayList<String> set = event.getChannel().getUserList();
        final String oldFormattedNick = event.getOldNick();
        final String newFormattedNick = event.getNewNick();

        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
        set.set(set.indexOf(oldFormattedNick), newFormattedNick);
        Collections.sort(set, new UserComparator());
    }

    @Override
    public void onTopic(final TopicEvent<LightBot> event) {
        event.getChannel().appendToBuffer(Utils.getOutputForEvent(event));
    }
}