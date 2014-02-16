package com.fusionx.lightirc.model;

import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;
import com.fusionx.relay.interfaces.SubServerObject;

import java.util.ArrayList;
import java.util.List;

public class WrappedServerListItem {

    private final ServerConfiguration.Builder mBuilder;

    private final List<SubServerObject> mServerObjects;

    private Server mServer;

    public WrappedServerListItem(final ServerConfiguration.Builder builder, final Server server) {
        mBuilder = builder;
        mServer = server;
        mServerObjects = new ArrayList<>();

        if (server != null) {
            mServerObjects.addAll(server.getUser().getChannels());
            mServerObjects.addAll(server.getUserChannelInterface().getPrivateMessageUsers());
        }
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

    public List<SubServerObject> getServerObjects() {
        return mServerObjects;
    }
}