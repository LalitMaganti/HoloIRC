package com.fusionx.lightirc.communication;

import com.fusionx.lightirc.model.MessagePriority;
import com.fusionx.lightirc.ui.ChannelFragment;
import com.fusionx.relay.Channel;
import com.fusionx.relay.PrivateMessageUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.event.NewPrivateMessage;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.event.channel.WorldMessageEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.user.UserEvent;
import com.fusionx.relay.interfaces.Conversation;

import java.util.HashMap;

public final class EventPriorityHelper {

    private static final int EVENT_PRIORITY = 100;

    private final HashMap<String, MessagePriority> mMessagePriorityMap;

    private final HashMap<String, Event> mEventMap;

    private final Server mServer;

    private final Callback mCallback;

    private MessagePriority mMessagePriority;

    public EventPriorityHelper(final Server server, final Callback callback) {
        mServer = server;
        mMessagePriorityMap = new HashMap<>();
        mEventMap = new HashMap<>();
        mCallback = callback;

        server.getServerEventBus().register(this, EVENT_PRIORITY);
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

    public MessagePriority getMessagePriority() {
        return mMessagePriority;
    }

    public void setMessagePriority(final MessagePriority priority) {
        if (mMessagePriority == null || mMessagePriority.compareTo(priority) < 0) {
            mMessagePriority = priority;
        }
    }

    public void onIRCEvent(final MessagePriority priority, final Conversation conversation,
            final Event event) {
        if (!conversation.equals(mCallback.getConversation())) {
            if (conversation.equals(conversation.getServer())) {
                setMessagePriority(priority);
            } else {
                setSubMessagePriority(conversation.getId(), priority);
                setSubEvent(conversation.getId(), event);
            }
        } else {
            if (!conversation.equals(conversation.getServer())) {
                setSubEvent(conversation.getId(), event);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final NewPrivateMessage event) {
        final PrivateMessageUser user = mServer.getUserChannelInterface().getPrivateMessageUser(
                event.nick);
        onIRCEvent(MessagePriority.HIGH, user, user.getBuffer().get(user.getBuffer().size() - 1));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final JoinEvent event) {
        final Channel channel = mServer.getUserChannelInterface().getChannel(event
                .channelName);
        onIRCEvent(MessagePriority.LOW, channel, channel.getBuffer().get(channel.getBuffer()
                .size() - 1));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final ChannelEvent event) {
        if (!ChannelFragment.sClasses.contains(event.getClass())) {
            final Conversation conversation = mServer.getUserChannelInterface()
                    .getChannel(event.channelName);

            if (event instanceof WorldMessageEvent) {
                onIRCEvent(MessagePriority.MEDIUM, conversation, event);
            } else {
                onIRCEvent(MessagePriority.LOW, conversation, event);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final UserEvent event) {
        if (!ChannelFragment.sClasses.contains(event.getClass())) {
            final Conversation conversation = mServer.getUserChannelInterface()
                    .getPrivateMessageUser(event.user.getNick());

            onIRCEvent(MessagePriority.HIGH, conversation, event);
        }
    }

    public interface Callback {

        public Conversation getConversation();
    }
}