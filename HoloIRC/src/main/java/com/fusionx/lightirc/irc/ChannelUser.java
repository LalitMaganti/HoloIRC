package com.fusionx.lightirc.irc;

import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.collections.UpdateableTreeSet;
import com.fusionx.lightirc.constants.UserLevelEnum;
import com.fusionx.lightirc.util.ColourParserUtils;

import android.content.Context;
import android.text.Spanned;
import android.widget.Checkable;

import java.util.HashMap;
import java.util.Set;

public class ChannelUser extends User implements UpdateableTreeSet.Updateable, Checkable {

    private final HashMap<Channel, UserLevelEnum> mUserLevelMap;

    private final HashMap<Channel, Spanned> mChannelSpannedMap;

    final Server mServer;

    public ChannelUser(final String nick, final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);

        mUserLevelMap = new HashMap<Channel, UserLevelEnum>();
        mChannelSpannedMap = new HashMap<Channel, Spanned>();
        mServer = userChannelInterface.getServer();

        // Checkable interface impl
        mChecked = false;
    }

    public String getPrettyNick(final String channel) {
        return getPrettyNick(mUserChannelInterface.getChannel(channel));
    }

    public String getPrettyNick(final Channel channel) {
        return String.format(mColourCode, getUserPrefix(channel) + mNick);
    }

    public Spanned getSpannedNick(final Channel channel) {
        final Spanned spannable = mChannelSpannedMap.get(channel);
        if (spannable == null) {
            updateSpannedNick(channel);
        }
        return spannable;
    }

    public String getBracketedNick(final Channel channel) {
        return String.format(mColourCode, "<" + getUserPrefix(channel) + mNick + ">");
    }

    public void onJoin(final Channel channel) {
        mUserLevelMap.put(channel, UserLevelEnum.NONE);
        updateSpannedNick(channel);
    }

    public void onRemove(final Channel channel) {
        mUserLevelMap.remove(channel);
        mChannelSpannedMap.remove(channel);
    }

    public UserLevelEnum getChannelPrivileges(final Channel channel) {
        return mUserLevelMap.get(channel);
    }

    private void updateSpannedNick(final Channel channel) {
        Spanned spannable = ColourParserUtils.parseMarkup(getPrettyNick(channel));
        mChannelSpannedMap.put(channel, spannable);
    }

    public char getUserPrefix(final Channel channel) {
        final UserLevelEnum level = mUserLevelMap.get(channel);
        if (UserLevelEnum.OP.equals(level)) {
            return '@';
        } else if (UserLevelEnum.VOICE.equals(level)) {
            return '+';
        } else {
            return '\0';
        }
    }

    public Set<Channel> getChannels() {
        return mUserChannelInterface.getAllChannelsInUser(this);
    }

    public void putMode(final UserLevelEnum mode, final Channel channel) {
        mUserLevelMap.put(channel, mode);
        updateSpannedNick(channel);
    }

    public void processWhoMode(final String rawMode, final Channel channel) {
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
        mUserLevelMap.put(channel, mode);
        updateSpannedNick(channel);
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
                        if (mUserLevelMap.get(channel) == UserLevelEnum.VOICE) {
                            channel.decrementVoices();
                        }
                        channel.incrementOps();
                        channel.getUsers().update(this, ImmutableList.of(channel,
                                UserLevelEnum.OP));
                        break;
                    }
                case 'v':
                    if (addingMode && !mUserLevelMap.get(channel).equals(UserLevelEnum.OP)) {
                        channel.incrementVoices();
                        channel.getUsers().update(this,
                                ImmutableList.of(channel, UserLevelEnum.VOICE));
                        break;
                    }
                default:
                    if (!addingMode && (character == 'v' || character == 'o')) {
                        if (character == 'o') {
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

        updateSpannedNick(channel);

        final String formattedSenderNick;
        final ChannelUser sendingUser = mUserChannelInterface.getUserIfExists(sendingNick);
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
        throw new RuntimeException();
    }

    @Override
    public void update(Object newValue) {
        if (newValue instanceof ImmutableList) {
            // ArrayList = mode change
            ImmutableList list = (ImmutableList) newValue;
            if (list.get(0) instanceof Channel && list.get(1) instanceof UserLevelEnum) {
                mUserLevelMap.put((Channel) list.get(0), (UserLevelEnum) list.get(1));
            }
        } else if (newValue instanceof Channel) {
            updateSpannedNick((Channel) newValue);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ChannelUser) {
            final ChannelUser us = (ChannelUser) o;
            return us.mNick.equals(mNick) && us.mServer.equals(mServer);
        } else {
            return false;
        }
    }

    // Checkable interface implementation
    private boolean mChecked;

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