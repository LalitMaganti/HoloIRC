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

package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.irc.LightUser;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;

public class Utils {
    public static String getOutputForEvent(Event e) {
        if (e instanceof MotdEvent) {
            MotdEvent event = (MotdEvent) e;
            return event.getMotd() + "\n";
        } else if (e instanceof NoticeEvent) {
            NoticeEvent event = (NoticeEvent) e;
            return event.getNotice() + "\n";
        } else if (e instanceof ActionEvent) {
            ActionEvent event = (ActionEvent) e;
            return "* " + event.getUser().getNick() + " " + event.getAction() + "\n";
        } else if (e instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) e;
            return event.getUser().getNick() + " entered the room\n";
        } else if (e instanceof TopicEvent) {
            TopicEvent event = (TopicEvent) e;
            String newMessage;
            if (event.isChanged()) {
                newMessage = "The topic for this channel has been changed to: "
                        + event.getTopic()
                        + " by "
                        + event.getChannel().getTopicSetter() + "\n";
            } else {
                newMessage = "The topic is: "
                        + event.getTopic() + " as set forth by "
                        + event.getChannel().getTopicSetter() + "\n";
            }
            return newMessage;
        } else if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) e;
            return event.getUser().getNick() + ": " + event.getMessage() + "\n";
        } else if (e instanceof QuitEvent) {
            QuitEvent event = (QuitEvent) e;
            return event.getUser().getNick() + " quit the room\n";
        } else if (e instanceof PartEvent) {
            PartEvent event = (PartEvent) e;
            return event.getUser().getNick() + " parted from the room\n";
        } else if (e instanceof NickChangeEvent) {
            NickChangeEvent event = (NickChangeEvent) e;
            String newMessage;
            if (!event.getUser().equals(event.getBot().getUserBot())) {
                newMessage = event.getOldNick() + " is now known as "
                        + event.getNewNick() + "\n";
            } else {
                newMessage = "You (" + event.getOldNick() + ") are now known as "
                        + event.getNewNick() + "\n";
            }
            ((LightUser) (event.getUser())).setTrueNick(event.getNewNick());
            return newMessage;
        } else {
            // Invalid event
            return "";
        }
    }
}