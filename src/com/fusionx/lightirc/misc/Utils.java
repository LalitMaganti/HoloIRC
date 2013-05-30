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

import android.graphics.Color;
import com.fusionx.lightirc.irc.LightUser;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.events.lightirc.NickChangeEventPerChannel;
import org.pircbotx.hooks.events.lightirc.QuitEventPerChannel;

import java.util.Random;

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
            LightUser user = (LightUser) event.getUser();
            return "* " + user.getPrettyNick() + " " + event.getAction() + "\n";
        } else if (e instanceof JoinEvent) {
            JoinEvent event = (JoinEvent) e;
            LightUser user = (LightUser) event.getUser();
            return user.getPrettyNick() + " entered the room\n";
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
            LightUser user = (LightUser) event.getUser();
            return user.getPrettyNick() + ": " + event.getMessage() + "\n";
        } else if (e instanceof QuitEventPerChannel) {
            QuitEventPerChannel event = (QuitEventPerChannel) e;
            return event.getUser().getNick() + " quit the room\n";
        } else if (e instanceof PartEvent) {
            PartEvent event = (PartEvent) e;
            return event.getUser().getNick() + " parted from the room\n";
        } else if (e instanceof NickChangeEvent) {
            NickChangeEvent event = (NickChangeEvent) e;
            String newMessage;
            newMessage = "You (" + event.getOldNick() + ") are now known as "
                    + event.getNewNick() + "\n";
            ((LightUser) (event.getUser())).setTrueNick(event.getNewNick());
            return newMessage;
        } else if (e instanceof NickChangeEventPerChannel) {
            NickChangeEventPerChannel event = (NickChangeEventPerChannel) e;
            String newMessage;
            newMessage = event.getOldNick() + " is now known as "
                    + event.getNewNick() + "\n";
            ((LightUser) (event.getUser())).setTrueNick(event.getNewNick());
            return newMessage;
        } else {
            // Invalid event
            return "";
        }
    }

    public static int generateRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red) / 2;
        green = (green) / 2;
        blue = (blue) / 2;

        int color = Color.rgb(red, green, blue);
        return color;
    }

}