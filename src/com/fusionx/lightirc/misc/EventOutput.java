package com.fusionx.lightirc.misc;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;

public class EventOutput {
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
            return newMessage;
        } else {
            // Invalid event
            return "";
        }
    }
}