package com.fusionx.lightirc.model;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.function.Optionals;

import static co.fusionx.relay.base.ServerConfiguration.Builder;

public class ServerWrapper {

    private final Builder mBuilder;

    private final Set<Conversation> mServerObjects;

    private final Collection<String> mIgnoreList;

    private Server mServer;

    public ServerWrapper(final Builder builder, final Collection<String> ignoreList,
            final Server server) {
        mBuilder = builder;
        mIgnoreList = ignoreList;
        mServerObjects = new LinkedHashSet<>();

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
        FluentIterable.from(server.getUser().getChannels()).copyInto(mServerObjects);
        FluentIterable.from(server.getUserChannelInterface().getQueryUsers())
                .copyInto(mServerObjects);
        FluentIterable.from(server.getDCCManager().getChatConversations())
                .copyInto(mServerObjects);
    }

    public Collection<String> getIgnoreList() {
        return mIgnoreList;
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public void addServerObject(final Conversation conversation) {
        mServerObjects.add(conversation);
    }

    public void removeConversation(final String id) {
        final Optional<Conversation> conversation =
                FluentIterable.from(mServerObjects)
                        .filter(c -> id.equals(c.getId()))
                        .first();
        Optionals.ifPresent(conversation, mServerObjects::remove);
    }

    public void removeConversation(final Conversation conversation) {
        mServerObjects.remove(conversation);
    }

    public int getSubServerSize() {
        return mServerObjects.size();
    }

    public Conversation getSubServer(int childPos) {
        final Iterator<Conversation> iterator = mServerObjects.iterator();
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
        mServerObjects.clear();
    }
}