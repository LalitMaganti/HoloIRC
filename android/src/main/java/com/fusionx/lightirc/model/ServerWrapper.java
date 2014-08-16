package com.fusionx.lightirc.model;

import co.fusionx.relay.Channel;
import co.fusionx.relay.ConnectionStatus;
import co.fusionx.relay.Conversation;
import co.fusionx.relay.QueryUser;
import co.fusionx.relay.Server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static co.fusionx.relay.ServerConfiguration.Builder;

public class ServerWrapper {

    private final Builder mBuilder;

    private final HashMap<String, Conversation> mServerObjects;

    private final Collection<String> mIgnoreList;

    private Server mServer;

    public ServerWrapper(final Builder builder, final Collection<String> ignoreList,
            final Server server) {
        mBuilder = builder;
        mIgnoreList = ignoreList;
        mServerObjects = new LinkedHashMap<>();

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

        if (isServerAvailable()) {
            for (final Channel c : server.getUser().getChannels()) {
                mServerObjects.put(c.getName(), c);
            }
            for (final QueryUser u : server.getUserChannelInterface().getQueryUsers()) {
                mServerObjects.put(u.getNick().getNickAsString(), u);
            }
        }
    }

    public Collection<String> getIgnoreList() {
        return mIgnoreList;
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

    public void checkAndRemoveInvalidConversations() {
        for (Iterator<Conversation> iterator = mServerObjects.values().iterator();
                iterator.hasNext(); ) {
            final Conversation conversation = iterator.next();
            if (!conversation.isValid()) {
                iterator.remove();
            }
        }
    }

    public void removeAll() {
        mServerObjects.clear();
    }
}