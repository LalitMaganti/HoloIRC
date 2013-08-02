package com.fusionx.ircinterface.misc;

import android.content.Context;
import android.graphics.Color;
import com.fusionx.ircinterface.constants.Constants;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.events.Event;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.fusionx.lightirc.misc.Utils.themeIsHoloLight;

public class Utils {
    /**
     * Split the line received from the server into it's components
     *
     * @param rawLine the line received from the server
     * @return the parsed list
     */
    public static ArrayList<String> splitRawLine(final String rawLine) {
        if (rawLine != null) {
            final ArrayList<String> list = new ArrayList<>();
            String buffer = "";
            final String colonLessLine = rawLine.charAt(0) == ':' ? rawLine.substring(1) : rawLine;

            for (int i = 0; i < colonLessLine.length(); i++) {
                final char c = colonLessLine.charAt(i);
                if (c == ' ') {
                    list.add(buffer);
                    buffer = "";
                } else if (c == ':' && StringUtils.isEmpty(buffer)) {
                    // A colon can occur in an IPv6 address so that is why the buffer check
                    // is necessary - the final colon can only occur with an empty buffer
                    // Add all the stuff after the last colon as a single item
                    // Essentially the first colon that occurs when the buffer is empty
                    list.add(colonLessLine.substring(i + 1));
                    break;
                } else {
                    buffer += c;
                }
            }

            if (!StringUtils.isEmpty(buffer)) {
                list.add(buffer);
            }
            return list;
        } else {
            return null;
        }
    }

    public static String convertArrayListToString(final ArrayList<String> list) {
        final StringBuilder builder = new StringBuilder();
        for (final String item : list) {
            builder.append(item).append(" ");
        }
        return builder.toString().trim();
    }

    public static void removeFirstElementFromList(final List<String> list, final int noOfTimes) {
        for (int i = 1; i <= noOfTimes; i++) {
            list.remove(0);
        }
    }

    public static Event parcelDataForBroadcast(@NonNull final String destinationType,
                                               final String destination,
                                               @NonNull final Enum type,
                                               @NonNull final String... message) {
        final Event event = new Event();
        if (destination == null) {
            if (destinationType.equals(EventDestination.Server)
                    || destinationType.equals(EventDestination.Core)) {
                event.setDestination(destinationType);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            if (!(destinationType.equals(EventDestination.Server)
                    || destinationType.equals(EventDestination.Core))) {
                event.setDestination(destinationType + "." + destination);
            } else {
                throw new IllegalArgumentException();
            }
        }
        event.setType(type);
        event.setMessage(message);

        return event;
    }

    public static String getNickFromRaw(final String rawSource) {
        String nick;
        if (rawSource.contains("!") && rawSource.contains("@")) {
            final int indexOfExclamation = rawSource.indexOf('!');
            nick = StringUtils.left(rawSource, indexOfExclamation);
        } else {
            nick = rawSource;
        }
        return nick;
    }

    public static int generateRandomColor(final int colorOffset) {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red + colorOffset) / 2;
        green = (green + colorOffset) / 2;
        blue = (blue + colorOffset) / 2;

        return Color.rgb(red, green, blue);
    }

    public static int getUserColorOffset(final Context context) {
        return themeIsHoloLight(context) ? 0 : 255;
    }

    public static boolean isChannel(String rawName) {
        return Constants.channelPrefixes.contains(rawName.charAt(0));
    }
}
