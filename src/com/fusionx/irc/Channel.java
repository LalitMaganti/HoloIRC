package com.fusionx.irc;

import android.os.Bundle;
import android.os.Message;

import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.irc.handlerabstract.ChannelHandler;
import com.fusionx.irc.writers.ChannelWriter;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.uiircinterface.MessageSender;

import de.scrum_master.util.UpdateableTreeSet;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Data
@Setter(AccessLevel.PACKAGE)
public class Channel implements Comparable<Channel>, UpdateableTreeSet.Updateable {
    protected final String name;
    protected String buffer = "";

    @Setter(AccessLevel.PUBLIC)
    protected String topic;
    @Setter(AccessLevel.PUBLIC)
    protected String topicSetter;

    @Getter
    protected final ChannelWriter writer;
    @Getter(AccessLevel.NONE)
    protected final UserChannelInterface mUserChannelInterface;

    protected Channel(@NonNull final String channelName,
                      @NonNull final UserChannelInterface userChannelInterface) {
        name = channelName;
        writer = new ChannelWriter(userChannelInterface.getOutputStream(), this);
        mUserChannelInterface = userChannelInterface;

        final String message = String.format(userChannelInterface.getContext().getString(R.string
                .parser_joined_channel), userChannelInterface
                .getServer().getUser().getColorfulNick());
        buffer += message + "\n";

        final MessageSender sender = MessageSender.getSender(userChannelInterface.getServer()
                .getTitle());
        sender.registerChannelHandler(channelName, channelHandler);
    }

    @Override
    public int compareTo(final Channel channel) {
        return this.getName().compareTo(channel.getName());
    }

    public UpdateableTreeSet<User> getUsers() {
        return mUserChannelInterface.getAllUsersInChannel(this);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Channel && ((Channel) o).getName().equals(name);
    }

    @Override
    public void update() {
        throw new IllegalArgumentException();
    }

    @Override
    public void update(final Object newValue) {
        throw new IllegalArgumentException();
    }

    private ChannelHandler channelHandler = new ChannelHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle event = msg.getData();
            final ChannelEventType type = (ChannelEventType) event
                    .getSerializable(EventBundleKeys.eventType);
            switch (type) {
                case UserListChanged:
                    if (!Utils.isMessagesFromChannelShown(mUserChannelInterface.getContext())) {
                        break;
                    }
                case Generic:
                    buffer += event.getString(EventBundleKeys.message) + "\n";
            }
        }
    };
}