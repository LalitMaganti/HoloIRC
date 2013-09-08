package com.fusionx.lightirc.irc;

import android.content.Context;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.collections.UpdateableTreeSet;
import com.fusionx.lightirc.constants.UserLevelEnum;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;

import lombok.Data;
import lombok.NonNull;

@Data
public class ChannelUser extends User implements UpdateableTreeSet.Updateable {
    protected final HashMap<Channel, UserLevelEnum> userLevelMap = new HashMap<>();

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
        final UserLevelEnum level = userLevelMap.get(channel);
        if (UserLevelEnum.OP.equals(level)) {
            return "@";
        } else if (UserLevelEnum.VOICE.equals(level)) {
            return "+";
        } else {
            return "";
        }
    }

    public UpdateableTreeSet<Channel> getChannels() {
        return userChannelInterface.getAllChannelsInUser(this);
    }

    public UserLevelEnum processWhoMode(final String rawMode, final Channel channel) {
        UserLevelEnum mode = UserLevelEnum.NONE;
        if (rawMode.contains("~")) {
            mode = UserLevelEnum.OWNER;
        } else if (rawMode.contains("&")) {
            mode = UserLevelEnum.SUPEROP;
        } else if (rawMode.contains("@")) {
            mode = UserLevelEnum.OP;
        } else if (rawMode.contains("%")) {
            mode = UserLevelEnum.HALFOP;
        } else if (rawMode.contains("+")) {
            mode = UserLevelEnum.VOICE;
        }
        userLevelMap.put(channel, mode);
        return mode;
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
                        channel.getUsers().update(this, ImmutableList.of(channel,
                                UserLevelEnum.OP));
                        break;
                    }
                case 'v':
                    if (addingMode && !userLevelMap.get(channel).equals(UserLevelEnum.OP)) {
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevelEnum.VOICE));
                        break;
                    }
                default:
                    if (!addingMode && (character == 'v' || character == 'o')) {
                        channel.getUsers().update(this, ImmutableList.of(channel,
                                UserLevelEnum.NONE));
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
            if (list.get(0) instanceof Channel && list.get(1) instanceof UserLevelEnum) {
                userLevelMap.put((Channel) list.get(0), (UserLevelEnum) list.get(1));
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ChannelUser && ((ChannelUser) o).getNick().equals(nick);
    }
}