package com.fusionx.lightirc.model;

import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.interfaces.Conversation;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.fusionx.relay.ServerConfiguration.Builder;

public class ServerWrapper implements Parcelable {


    public static final Parcelable.Creator<ServerWrapper> CREATOR =
            new Parcelable.Creator<ServerWrapper>() {
                public ServerWrapper createFromParcel(final Parcel in) {
                    return new ServerWrapper(in);
                }

                public ServerWrapper[] newArray(final int size) {
                    return new ServerWrapper[size];
                }
            };

    private final Builder mBuilder;

    private final HashMap<String, Conversation> mServerObjects;

    private final HashMap<String, MessagePriority> mMessagePriorityMap;

    private final HashMap<String, Event> mEventMap;

    private MessagePriority mMessagePriority;

    private Server mServer;

    public ServerWrapper(final Builder builder, final Server server) {
        mBuilder = builder;
        mServerObjects = new LinkedHashMap<>();
        mMessagePriorityMap = new HashMap<>();
        mEventMap = new HashMap<>();

        setServer(server);
    }

    public ServerWrapper(Parcel in) {
        mBuilder = in.readParcelable(Builder.class.getClassLoader());
        mServerObjects = (HashMap<String, Conversation>) in.readSerializable();
        mMessagePriorityMap = (HashMap<String, MessagePriority>) in.readSerializable();
        mEventMap = (HashMap<String, Event>) in.readSerializable();
        mMessagePriority = (MessagePriority) in.readSerializable();
    }

    public boolean isConnected() {
        return mServer != null && mServer.getStatus() == ConnectionStatus.CONNECTED;
    }

    public String getTitle() {
        return mBuilder.getTitle();
    }

    public Server getServer() {
        return mServer;
    }

    public void setServer(final Server server) {
        mServer = server;

        if (isConnected()) {
            for (final Channel channel : server.getUser().getChannels()) {
                mServerObjects.put(channel.getName(), channel);
            }
            for (final PrivateMessageUser user : server.getUserChannelInterface()
                    .getPrivateMessageUsers()) {
                mServerObjects.put(user.getNick(), user);
            }
        }
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public void addServerObject(final Conversation conversation) {
        mServerObjects.put(conversation.getId(), conversation);
    }

    public void removeServerObject(final String id) {
        mServerObjects.remove(id);
    }

    public int getSubServerSize() {
        return mServerObjects.size();
    }

    public Conversation getSubServer(int childPos) {
        final Iterator<Conversation> iterator = mServerObjects.values().iterator();
        for (int i = 0; i < childPos; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public void setSubMessagePriority(final String title, final MessagePriority priority) {
        final MessagePriority oldPriority = mMessagePriorityMap.get(title);
        if (oldPriority == null || oldPriority.compareTo(priority) < 0) {
            mMessagePriorityMap.put(title, priority);
        }
    }

    public void setSubEvent(final String title, final Event event) {
        mEventMap.put(title, event);
    }

    public void clearMessagePriority() {
        mMessagePriority = null;
    }

    public void clearMessagePriority(Conversation conversation) {
        mMessagePriorityMap.remove(conversation.getId());
    }

    public MessagePriority getSubMessagePriority(final String title) {
        return mMessagePriorityMap.get(title);
    }

    public Event getSubEvent(final String id) {
        return mEventMap.get(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mBuilder, 0);
        dest.writeSerializable(mServerObjects);
        dest.writeSerializable(mMessagePriorityMap);
        dest.writeSerializable(mEventMap);
        dest.writeSerializable(mMessagePriority);
    }

    public MessagePriority getMessagePriority() {
        return mMessagePriority;
    }

    public void setMessagePriority(final MessagePriority priority) {
        if (mMessagePriority == null || mMessagePriority.compareTo(priority) < 0) {
            mMessagePriority = priority;
        }
    }
}