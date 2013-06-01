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

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.PrivateActionEvent;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

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
            return "* " + event.getUser().getPrettyNick(event.getChannel())
                    + " " + event.getAction() + "\n";
        } else if (e instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) e;
            return event.getUser().getPrettyNick(event.getChannel()) + " entered the room\n";
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
            return event.getUser().getPrettyNick(event.getChannel())
                    + ": " + event.getMessage() + "\n";
        } else if (e instanceof QuitEventPerChannel) {
            QuitEventPerChannel event = (QuitEventPerChannel) e;
            return event.getUser().getPrettyNick(event.getChannel()) + " quit the room\n";
        } else if (e instanceof PartEvent) {
            PartEvent event = (PartEvent) e;
            return event.getUser().getPrettyNick(event.getChannel()) + " parted from the room\n";
        } else if (e instanceof NickChangeEventPerChannel) {
            NickChangeEventPerChannel event = (NickChangeEventPerChannel) e;
            String newMessage;
            if (event.getUser().getNick().equals(event.getBot().getNick())) {
                newMessage = "You (" + event.getOldNick() + ") are now known as "
                        + event.getNewNick() + "\n";
            } else {
                newMessage = event.getOldNick() + " is now known as "
                        + event.getNewNick() + "\n";
            }
            return newMessage;
        } else if (e instanceof PrivateMessageEvent) {
            PrivateMessageEvent event = (PrivateMessageEvent) e;
            return event.getUser().getColourfulNick() + ": " + event.getMessage() + "\n";
        } else if (e instanceof PrivateActionEvent) {
            PrivateActionEvent event = (PrivateActionEvent) e;
            return "* " + event.getUser().getColourfulNick() + " " + event.getMessage() + "\n";
        } else {
            // Invalid event
            return "";
        }
    }
}