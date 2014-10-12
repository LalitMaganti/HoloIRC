package com.fusionx.lightirc.model;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import com.fusionx.relay.configuration.ParcelableConnectionConfiguration;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.core.Session;

public class SessionContainer {

    private final ParcelableConnectionConfiguration.Builder mBuilder;

    private final List<Conversation> mConversations;

    private Session mSession;

    public SessionContainer(final ParcelableConnectionConfiguration.Builder builder,
            final Optional<? extends Session> session) {
        mBuilder = builder;
        mConversations = new ArrayList<>();

        setSession(session);
    }

    public String getTitle() {
        return mBuilder.getTitle();
    }

    public Session getSession() {
        return mSession;
    }

    public void setSession(final Optional<? extends Session> optional) {
        mSession = optional.orNull();

        if (!optional.isPresent()) {
            return;
        }
        final Session session = optional.get();
        FluentIterable.from(session.getUserChannelManager().getUser().getChannels())
                .copyInto(mConversations);
        FluentIterable.from(session.getQueryManager().getQueryUsers())
                .copyInto(mConversations);
        // FluentIterable.from(session.getDCCManager().getChatConversations())
        //        .copyInto(mConversations);
        //FluentIterable.from(session.getDCCManager().getFileConversations())
        //        .copyInto(mConversations);
    }

    public ParcelableConnectionConfiguration.Builder getBuilder() {
        return mBuilder;
    }

    public void addConversation(final Conversation conversation) {
        mConversations.add(conversation);
    }

    public int removeConversation(final Conversation conversation) {
        final int position = mConversations.indexOf(conversation);
        mConversations.remove(position);
        return position;
    }

    public int getConversationCount() {
        return mConversations.size();
    }

    public Conversation getConversation(int childPos) {
        return mConversations.get(childPos);
    }

    public void refreshConversations() {
        removeAll();
        setSession(Optional.fromNullable(mSession));
    }

    public void removeAll() {
        mConversations.clear();
    }

    public int indexOf(final Conversation conversation) {
        return mConversations.indexOf(conversation);
    }

    public int removeSession() {
        final int size = mConversations.size();
        setSession(Optional.<Session>absent());
        mConversations.clear();

        return size;
    }
}