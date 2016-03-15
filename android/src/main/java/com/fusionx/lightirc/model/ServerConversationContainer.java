package com.fusionx.lightirc.model;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import co.fusionx.relay.ConnectionStatus;
import co.fusionx.relay.Conversation;
import co.fusionx.relay.Server;
import co.fusionx.relay.function.Optionals;

import static co.fusionx.relay.ServerConfiguration.Builder;

public class ServerConversationContainer {

    private final Builder mBuilder;

    private final Set<Conversation> mConversations;

    private final Collection<String> mIgnoreList;

    private Server mServer;

    public ServerConversationContainer(final Builder builder, final Collection<String> ignoreList,
            final Server server) {
        mBuilder = builder;
        mIgnoreList = ignoreList;
        mConversations = new LinkedHashSet<>();

        setServer(server);
    }

    public boolean isServerAvailable() {
        return mServer != null && (mServer.getStatus() == ConnectionStatus.CONNECTED
                || mServer.getStatus() == ConnectionStatus.RECONNECTING);
    }

    public String getTitle() {
        return mBuilder.getTitle();
    }

    public Server getServer() {
        return mServer;
    }

    public void setServer(final Server server) {
        mServer = server;

        if (server == null) {
            return;
        }
        FluentIterable.from(server.getUser().getChannels()).copyInto(mConversations);
        FluentIterable.from(server.getUserChannelInterface().getQueryUsers())
                .copyInto(mConversations);
        // FluentIterable.from(server.getDCCManager().getChatConversations())
        //         .copyInto(mConversations);
        // FluentIterable.from(server.getDCCManager().getFileConversations())
        //        .copyInto(mConversations);
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

    public void removeConversation(final String id) {
        final Optional<Conversation> conversation =
                FluentIterable.from(mConversations)
                        .filter(c -> id.equals(c.getId()))
                        .first();
        Optionals.ifPresent(conversation, mConversations::remove);
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
        setServer(mServer);
    }

    public void removeAll() {
        mConversations.clear();
    }
}