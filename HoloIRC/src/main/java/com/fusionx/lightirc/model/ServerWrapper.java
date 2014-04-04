package com.fusionx.lightirc.model;

import com.fusionx.relay.Channel;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.interfaces.Conversation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gnu.trove.map.hash.THashMap;

public class ServerWrapper {

    private final ServerConfiguration.Builder mBuilder;

    private final Map<String, Conversation> mServerObjects;

    private Server mServer;

    public ServerWrapper(final ServerConfiguration.Builder builder, final Server server) {
        mBuilder = builder;
        mServer = server;
        mServerObjects = new LinkedHashMap<>();

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
    }

    public ServerConfiguration.Builder getBuilder() {
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
}