package com.fusionx.lightirc.model;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import co.fusionx.relay.conversation.Channel;
import co.fusionx.relay.core.SessionStatus;
import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.core.Session;

import static co.fusionx.relay.core.ConnectionConfiguration.Builder;

public class SessionContainer {

    private final Builder mBuilder;

    private final List<Conversation> mConversations;

    private final Collection<String> mIgnoreList;

    private Session mSession;

    public SessionContainer(final Builder builder, final Collection<String> ignoreList,
            final Optional<Session> session) {
        mBuilder = builder;
        mIgnoreList = ignoreList;
        mConversations = new ArrayList<>();

        setSession(session);
    }

    public boolean isServerAvailable() {
        return mSession != null && (mSession.getStatus() == SessionStatus.CONNECTED
                || mSession.getStatus() == SessionStatus.RECONNECTING);
    }

    public String getTitle() {
        return mBuilder.getTitle();
    }

    public Session getSession() {
        return mSession;
    }

    public void setSession(final Optional<Session> optional) {
        mSession = optional.orNull();

        if (!optional.isPresent()) {
            return;
        }
        final Session session = optional.get();
        FluentIterable.from(session.getUserChannelManager().getUser().getChannels())
                .copyInto(mConversations);
        FluentIterable.from(session.getQueryManager().getQueryUsers())
                .copyInto(mConversations);
        FluentIterable.from(session.getDCCManager().getChatConversations())
                .copyInto(mConversations);
        FluentIterable.from(session.getDCCManager().getFileConversations())
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