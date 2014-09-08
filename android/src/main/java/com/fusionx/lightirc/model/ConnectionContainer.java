package com.fusionx.lightirc.model;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.IRCConnection;

import static co.fusionx.relay.base.ServerConfiguration.Builder;

public class ConnectionContainer {

    private final Builder mBuilder;

    private final Set<Conversation> mConversations;

    private final Collection<String> mIgnoreList;

    private IRCConnection mConnection;

    public ConnectionContainer(final Builder builder, final Collection<String> ignoreList,
            final Optional<IRCConnection> connection) {
        mBuilder = builder;
        mIgnoreList = ignoreList;
        mConversations = new LinkedHashSet<>();

        setConnection(connection.orNull());
    }

    public boolean isServerAvailable() {
        return mConnection != null && (mConnection.getStatus() == ConnectionStatus.CONNECTED
                || mConnection.getStatus() == ConnectionStatus.RECONNECTING);
    }

    public String getTitle() {
        return mBuilder.getTitle();
    }

    public IRCConnection getConnection() {
        return mConnection;
    }

    public void setConnection(final IRCConnection connection) {
        mConnection = connection;

        if (connection == null) {
            return;
        }
        FluentIterable.from(connection.getUserChannelDao().getUser().getChannels())
                .copyInto(mConversations);
        FluentIterable.from(connection.getUserChannelDao().getUser().getQueryUsers())
                .copyInto(mConversations);
        FluentIterable.from(connection.getServer().getDCCManager().getChatConversations())
                .copyInto(mConversations);
        FluentIterable.from(connection.getServer().getDCCManager().getFileConversations())
                .copyInto(mConversations);
    }

    public Collection<String> getIgnoreList() {
        return mIgnoreList;
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public void addConversation(final Conversation conversation) {
        mConversations.add(conversation);
    }

    public void removeConversation(final Conversation conversation) {
        mConversations.remove(conversation);
    }

    public int getConversationCount() {
        return mConversations.size();
    }

    public Conversation getConversation(int childPos) {
        final Iterator<Conversation> iterator = mConversations.iterator();
        for (int i = 0; i < childPos; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public void refreshConversations() {
        removeAll();
        setConnection(mConnection);
    }

    public void removeAll() {
        mConversations.clear();
    }
}