package com.fusionx.ircinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.UserEventType;
import com.fusionx.ircinterface.enums.UserLevel;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.misc.Utils;
import com.fusionx.ircinterface.writers.UserWriter;
import com.fusionx.lightirc.R;
import com.google.common.collect.ImmutableList;
import de.scrum_master.util.UpdateableTreeSet;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;

@Data
public class User implements UpdateableTreeSet.Updateable {
    protected String nick;
    protected String login;
    protected String host;
    protected String serverUrl;
    protected String realName;

    protected final UserBufferListener listener;
    protected String buffer = "";

    @Getter(AccessLevel.NONE)
    protected String nickHTML;

    @Getter(AccessLevel.NONE)
    protected final UserChannelInterface userChannelInterface;
    protected final HashMap<Channel, UserLevel> userLevelMap = new HashMap<>();

    @Getter
    protected final UserWriter writer;

    public User(@NonNull final String nick,
                @NonNull final UserChannelInterface userChannelInterface) {
        this.nick = nick;
        this.writer = new UserWriter(userChannelInterface.getOutputStream(), this);
        this.userChannelInterface = userChannelInterface;

        nickHTML = "<font color=\"" + Utils.generateRandomColor(Utils
                .getUserColorOffset(userChannelInterface.getContext())) + "\">%1$s</font>";

        final IntentFilter filter = new IntentFilter();
        filter.addAction(EventDestination.User + "." + nick);

        listener = new UserBufferListener();
        userChannelInterface.getManager().registerReceiver(listener, filter);

        userChannelInterface.putAppUser(this);
    }

    public UpdateableTreeSet<Channel> getChannels() {
        return userChannelInterface.getAllChannelsInUser(this);
    }

    public String getColorfulNick() {
        return String.format(nickHTML, nick);
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

    @Override
    public boolean equals(Object o) {
        return o instanceof User && ((User) o).getNick().equals(nick);
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
        final User sendingUser = userChannelInterface.getUserIfExists(sendingNick);
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

    private class UserBufferListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Event event = intent.getParcelableExtra("event");
            if (event != null) {
                final UserEventType type = (UserEventType) event.getType();
                switch (type) {
                    case Generic:
                        buffer += event.getMessage()[0] + "\n";
                }
            }
        }
    }
}