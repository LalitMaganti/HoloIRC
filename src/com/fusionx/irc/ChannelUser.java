package com.fusionx.irc;

import android.content.Context;

import com.fusionx.irc.enums.UserLevel;
import com.fusionx.lightirc.R;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;

import de.scrum_master.util.UpdateableTreeSet;
import lombok.Data;
import lombok.NonNull;

@Data
public class ChannelUser extends User implements UpdateableTreeSet.Updateable {
    protected String login;
    protected String host;
    protected String serverUrl;
    protected String realName;

    protected final HashMap<Channel, UserLevel> userLevelMap = new HashMap<>();

    public ChannelUser(@NonNull String nick, @NonNull UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
    }

    public String getPrettyNick(final String channel) {
        return getPrettyNick(userChannelInterface.getChannel(channel));
    }

    public String getPrettyNick(final Channel channel) {
        return String.format(nickHTML, getUserPrefix(channel) + nick);
    }

    protected String getUserPrefix(final Channel channel) {
        final UserLevel level = userLevelMap.get(channel);
        if (UserLevel.OP.equals(level)) {
            return "@";
        } else if (UserLevel.VOICE.equals(level)) {
            return "+";
        } else {
            return "";
        }
    }

    public UpdateableTreeSet<Channel> getChannels() {
        return userChannelInterface.getAllChannelsInUser(this);
    }

    public void processWhoMode(final String rawMode, final Channel channel) {
        UserLevel mode = UserLevel.NONE;
        if (rawMode.contains("~")) {
            mode = UserLevel.OWNER;
        } else if (rawMode.contains("&")) {
            mode = UserLevel.SUPEROP;
        } else if (rawMode.contains("@")) {
            mode = UserLevel.OP;
        } else if (rawMode.contains("%")) {
            mode = UserLevel.HALFOP;
        } else if (rawMode.contains("+")) {
            mode = UserLevel.VOICE;
        }
        userLevelMap.put(channel, mode);
    }

    public String processModeChange(final Context context, final String sendingNick,
                                    final Channel channel, final String mode) {
        boolean addingMode = false;
        for (char character : mode.toCharArray()) {
            switch (character) {
                case '+':
                    addingMode = true;
                    break;
                case '-':
                    addingMode = false;
                    break;
                case 'o':
                    if (addingMode) {
                        channel.getUsers().update(this, ImmutableList.of(channel, UserLevel.OP));
                        break;
                    }
                case 'v':
                    if (addingMode) {
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevel.VOICE));
                        break;
                    }
                default:
                    if (!addingMode && (character == 'v' || character == 'o')) {
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevel.NONE));
                    }
                    break;
            }
        }

        final String formattedSenderNick;
        final ChannelUser sendingUser = userChannelInterface.getUserIfExists(sendingNick);
        if (sendingUser == null) {
            formattedSenderNick = sendingNick;
        } else {
            formattedSenderNick = sendingUser.getPrettyNick(channel);
        }

        return String.format(context.getString(R.string.parser_mode_changed), mode,
                getColorfulNick(), formattedSenderNick);
    }

    @Override
    public void update() {
        // used for nick changes
    }

    @Override
    public void update(Object newValue) {
        if (newValue instanceof ImmutableList) {
            // ArrayList = mode change
            ImmutableList list = (ImmutableList) newValue;
            if (list.get(0) instanceof Channel && list.get(1) instanceof UserLevel) {
                userLevelMap.put((Channel) list.get(0), (UserLevel) list.get(1));
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ChannelUser && ((ChannelUser) o).getNick().equals(nick);
    }
}