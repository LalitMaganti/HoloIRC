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

import android.content.Context;
import com.fusionx.lightirc.R;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.*;

public class EventParser {
    public static String getOutputForEvent(final Event e, final Context context) {
        String returnMessage;
        if (e instanceof MotdEvent) {
            final MotdEvent event = (MotdEvent) e;
            returnMessage = event.getMotd();
        } else if (e instanceof NoticeEvent) {
            final NoticeEvent event = (NoticeEvent) e;
            returnMessage = event.getNotice();
        } else if (e instanceof ActionEvent) {
            final ActionEvent event = (ActionEvent) e;
            if (event.getChannel() == null) {
                returnMessage = "* " + event.getUser().getColourfulNick() + " " + event.getAction();
            } else {
                returnMessage = "* " + event.getUser().getPrettyNick(event.getChannel())
                        + " " + event.getAction();
            }
        } else if (e instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) e;
            returnMessage = event.getUser().getPrettyNick(event.getChannel())
                    + " " + context.getString(R.string.output_event_entered_room);
        } else if (e instanceof TopicEvent) {
            returnMessage = topicEventOutput((TopicEvent) e, context);
        } else if (e instanceof MessageEvent) {
            returnMessage = messageEventOutput((MessageEvent) e);
        } else if (e instanceof QuitEventPerChannel) {
            final QuitEventPerChannel event = (QuitEventPerChannel) e;
            returnMessage = event.getUser().getPrettyNick(event.getChannel()) + " "
                    + context.getString(R.string.output_event_quit_server);
            if (event.getReason() != null && !event.getReason().isEmpty()) {
                returnMessage += " " + context.getString(R.string.output_event_reason) + " " + event.getReason() + ")";
            }
        } else if (e instanceof PartEvent) {
            final PartEvent event = (PartEvent) e;
            returnMessage = event.getUser().getPrettyNick(event.getChannel()) + " "
                    + context.getString(R.string.output_event_part_channel);
            if (event.getReason() != null && !event.getReason().isEmpty()) {
                returnMessage += " " + context.getString(R.string.output_event_reason) + " " + event.getReason() + ")";
            }
        } else if (e instanceof NickChangeEventPerChannel) {
            returnMessage = nickChangeEventOutput((NickChangeEventPerChannel) e, context);
        } else if (e instanceof PrivateMessageEvent) {
            returnMessage = privateMessageEventOutput((PrivateMessageEvent) e);
        } else if (e instanceof IOExceptionEvent) {
            final IOExceptionEvent event = (IOExceptionEvent) e;
            returnMessage = event.getException().getMessage();
            //+ "\n" + context.getString(R.string.output_event_trying_reconnect);
        } else if (e instanceof IrcExceptionEvent) {
            final IrcExceptionEvent event = (IrcExceptionEvent) e;
            returnMessage = event.getException().getMessage();
        } else if (e instanceof ConnectEvent) {
            final ConnectEvent event = (ConnectEvent) e;
            returnMessage = context.getString(R.string.output_event_connected_to_server)
                    + " " + event.getBot().getConfiguration().getServerHostname();
        } else if (e instanceof DisconnectEvent) {
            final DisconnectEvent event = (DisconnectEvent) e;
            returnMessage = context.getString(R.string.output_event_disconnected_from)
                    + " " + event.getBot().getConfiguration().getServerHostname();
        } else if (e instanceof ModeEvent) {
            final ModeEvent event = (ModeEvent) e;
            returnMessage = context.getString(R.string.mode) + " " + event.getMode() + " " +
                    context.getString(R.string.by) + " " + event.getUser().getNick();
        } else if (e instanceof UnknownEvent) {
            final UnknownEvent event = (UnknownEvent) e;
            returnMessage = context.getString(R.string.output_event_unknown_event) + " " + event.getLine();
        } else if (e instanceof NickInUseEvent) {
            final NickInUseEvent event = (NickInUseEvent) e;
            returnMessage = event.getMessage();
        } else {
            // Invalid event
            return "";
        }
        return returnMessage + "\n";
    }

    private static String topicEventOutput(final TopicEvent event, final Context context) {
        String newMessage;
        if (event.isChanged()) {
            newMessage = context.getString(R.string.output_event_topic_changed_to) + " " + event.getTopic()
                    + " " + context.getString(R.string.output_event_by) + " " + event.getChannel().getTopicSetter();
        } else {
            newMessage = context.getString(R.string.output_event_topic_is) + " " + event.getTopic() + " " +
                    context.getString(R.string.output_event_topic_set_forth) + " " + event.getChannel().getTopicSetter();
        }
        return newMessage;
    }

    private static String messageEventOutput(final MessageEvent event) {
        String baseMessage = event.getUser().getPrettyNick(event.getChannel())
                + ": " + event.getMessage();
        if (event.getMessage().contains(event.getBot().getNick()) &&
                !event.getUser().getNick().equals(event.getBot().getNick())) {
            baseMessage = "<b>" + baseMessage + "</b>";
        }
        return baseMessage;
    }

    private static String nickChangeEventOutput(final NickChangeEventPerChannel event, final Context context) {
        String newMessage;
        if (event.getUser().getNick().equals(event.getBot().getNick())) {
            newMessage = context.getString(R.string.output_event_you) + event.getOldNick() +
                    context.getString(R.string.output_event_you_known_as) + " " + event.getNewNick();
        } else {
            newMessage = event.getOldNick() + " " + context.getString(R.string.output_event_known_as)
                    + " " + event.getNewNick();
        }
        return newMessage;
    }

    private static String privateMessageEventOutput(final PrivateMessageEvent event) {
        if (event.isBotMessage()) {
            return event.getBot().getUserBot().getColourfulNick() + ": " + event.getMessage();
        } else {
            return event.getUser().getColourfulNick() + ": " + event.getMessage();
        }
    }
}