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

package com.fusionx.lightirc.parser;

import com.fusionx.lightirc.irc.IOExceptionEvent;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

public class EventParser {
    public static String getOutputForEvent(final Event e) {
        if (e instanceof MotdEvent) {
            final MotdEvent event = (MotdEvent) e;
            return event.getMotd() + "\n";
        } else if (e instanceof NoticeEvent) {
            final NoticeEvent event = (NoticeEvent) e;
            return event.getNotice() + "\n";
        } else if (e instanceof ActionEvent) {
            final ActionEvent event = (ActionEvent) e;
            if (event.getChannel() == null) {
                return "* " + event.getUser().getColourfulNick() + " " + event.getAction() + "\n";
            } else {
                return "* " + event.getUser().getPrettyNick(event.getChannel())
                        + " " + event.getAction() + "\n";
            }
        } else if (e instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) e;
            return event.getUser().getPrettyNick(event.getChannel()) + " entered the room\n";
        } else if (e instanceof TopicEvent) {
            return topicEventOutput((TopicEvent) e);
        } else if (e instanceof MessageEvent) {
            return messageEventOutput((MessageEvent) e);
        } else if (e instanceof QuitEventPerChannel) {
            final QuitEventPerChannel event = (QuitEventPerChannel) e;
            return event.getUser().getPrettyNick(event.getChannel()) + " quit the server (Reason: "
                    + event.getReason() + ")\n";
        } else if (e instanceof PartEvent) {
            final PartEvent event = (PartEvent) e;
            return event.getUser().getPrettyNick(event.getChannel()) + " parted from the room (Reason: "
                    + event.getReason() + ")\n";
        } else if (e instanceof NickChangeEventPerChannel) {
            return nickChangeEventOutput((NickChangeEventPerChannel) e);
        } else if (e instanceof PrivateMessageEvent) {
            return privateMessageEventOutput((PrivateMessageEvent) e);
        } else if (e instanceof IOExceptionEvent) {
            final IOExceptionEvent event = (IOExceptionEvent) e;
            return event.getException().getMessage() + "\nTrying to reconnect in 5 seconds\n";
        } else {
            // Invalid event
            return "";
        }
    }

    private static String topicEventOutput(final TopicEvent event) {
        String newMessage;
        if (event.isChanged()) {
            newMessage = "The topic for this channel has been changed to: " + event.getTopic()
                    + " by " + event.getChannel().getTopicSetter() + "\n";
        } else {
            newMessage = "The topic is: " + event.getTopic() + " as set forth by "
                    + event.getChannel().getTopicSetter() + "\n";
        }
        return newMessage;
    }

    private static String messageEventOutput(final MessageEvent event) {
        String baseMessage = event.getUser().getPrettyNick(event.getChannel())
                + ": " + event.getMessage() + "\n";
        if (event.getMessage().contains(event.getBot().getNick()) &&
                !event.getUser().getNick().equals(event.getBot().getNick())) {
            baseMessage = "<b>" + baseMessage + "</b>";
        }
        return baseMessage;
    }

    private static String nickChangeEventOutput(final NickChangeEventPerChannel event) {
        String newMessage;
        if (event.getUser().getNick().equals(event.getBot().getNick())) {
            newMessage = "You (" + event.getOldNick() + ") are now known as "
                    + event.getNewNick() + "\n";
        } else {
            newMessage = event.getOldNick() + " is now known as "
                    + event.getNewNick() + "\n";
        }
        return newMessage;
    }

    private static String privateMessageEventOutput(final PrivateMessageEvent event) {
        if (event.isBotMessage()) {
            return event.getBot().getUserBot().getColourfulNick() + ": " + event.getMessage() + "\n";
        } else {
            return event.getUser().getColourfulNick() + ": " + event.getMessage() + "\n";
        }
    }
}