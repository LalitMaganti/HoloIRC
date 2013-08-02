package com.fusionx.ircinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ChannelEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.writers.ChannelWriter;
import com.fusionx.lightirc.misc.Utils;
import de.scrum_master.util.UpdateableTreeSet;
import lombok.*;

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
                      @NonNull final UserChannelInterface userChannelInterface, Context context) {
        name = channelName;
        writer = new ChannelWriter(userChannelInterface.getOutputStream(), this);
        mUserChannelInterface = userChannelInterface;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(EventDestination.Channel + "." + name);
        final ChannelBufferListener listener = new ChannelBufferListener();
        userChannelInterface.getManager().registerReceiver(listener, filter);
    }

    @Override
    public int compareTo(Channel channel) {
        return this.getName().compareTo(channel.getName());
    }

    public UpdateableTreeSet<User> getUsers() {
        return mUserChannelInterface.getAllUsersInChannel(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Channel && ((Channel) o).getName().equals(name);
    }

    @Override
    public void update() {
        throw new IllegalArgumentException();
    }

    @Override
    public void update(Object newValue) {
        throw new IllegalArgumentException();
    }

    private class ChannelBufferListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Event event = intent.getParcelableExtra("event");
            final ChannelEventType type = (ChannelEventType) event.getType();
            switch (type) {
                case UserListChanged:
                    if(!Utils.isMessagesFromChannelShown(mUserChannelInterface.getContext())) {
                        break;
                    }
                case Generic:
                    buffer += event.getMessage()[0] + "\n";
            }
        }
    }
}