package com.fusionx.lightirc.irc;

import android.content.Context;
import android.widget.Checkable;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.collections.UpdateableTreeSet;
import com.fusionx.lightirc.constants.UserLevelEnum;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;

import lombok.Data;
import lombok.NonNull;

@Data
public class ChannelUser extends User implements UpdateableTreeSet.Updateable, Checkable {
    protected final HashMap<Channel, UserLevelEnum> userLevelMap = new HashMap<>();
    private boolean mChecked = false;

    public ChannelUser(@NonNull String nick, @NonNull UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
    }

    public String getPrettyNick(final String channel) {
        return getPrettyNick(userChannelInterface.getChannel(channel));
    }

    public String getPrettyNick(final Channel channel) {
        return String.format(nickHTML, getUserPrefix(channel) + nick);
    }

    public char getUserPrefix(final Channel channel) {
        final UserLevelEnum level = userLevelMap.get(channel);
        if (UserLevelEnum.OP.equals(level)) {
            return '@';
        } else if (UserLevelEnum.VOICE.equals(level)) {
            return '+';
        } else {
            return '\0';
        }
    }

    public UpdateableTreeSet<Channel> getChannels() {
        return userChannelInterface.getAllChannelsInUser(this);
    }

    public UserLevelEnum processWhoMode(final String rawMode, final Channel channel) {
        UserLevelEnum mode = UserLevelEnum.NONE;
        // TODO - fix this up
        if (rawMode.contains("~")) {
            //mode = UserLevelEnum.OWNER;
            mode = UserLevelEnum.OP;
            channel.incrementOps();
        } else if (rawMode.contains("&")) {
            //mode = UserLevelEnum.SUPEROP;
            mode = UserLevelEnum.OP;
            channel.incrementOps();
        } else if (rawMode.contains("@")) {
            mode = UserLevelEnum.OP;
            channel.incrementOps();
        } else if (rawMode.contains("%")) {
            //mode = UserLevelEnum.HALFOP;
            mode = UserLevelEnum.VOICE;
            channel.incrementVoices();
        } else if (rawMode.contains("+")) {
            mode = UserLevelEnum.VOICE;
            channel.incrementVoices();
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
                        if(userLevelMap.get(channel) == UserLevelEnum.VOICE) {
                            channel.decrementVoices();
                        }
                        channel.getUsers().update(this, ImmutableList.of(channel,
                                UserLevelEnum.OP));
                        break;
                    }
                case 'v':
                    if (addingMode && !userLevelMap.get(channel).equals(UserLevelEnum.OP)) {
                        channel.incrementVoices();
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevelEnum.VOICE));
                        break;
                    }
                default:
                    if (!addingMode && (character == 'v' || character == 'o')) {
                        if(character == 'o') {
                            channel.decrementOps();
                        } else {
                            channel.decrementVoices();
                        }
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

    @Override
    public void setChecked(boolean b) {
        mChecked = b;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
    }
}