package com.fusionx.lightirc.service;

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.model.MessagePriority;
import com.fusionx.relay.Channel;
import com.fusionx.relay.QueryUser;
import com.fusionx.relay.Server;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.event.channel.ChannelEvent;
import com.fusionx.relay.event.channel.ChannelWorldActionEvent;
import com.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import com.fusionx.relay.event.channel.ChannelWorldUserEvent;
import com.fusionx.relay.event.server.JoinEvent;
import com.fusionx.relay.event.server.NewPrivateMessage;
import com.fusionx.relay.event.query.QueryEvent;
import com.fusionx.relay.interfaces.Conversation;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;

import static com.fusionx.lightirc.util.EventUtils.getLastStorableEvent;
import static com.fusionx.lightirc.util.EventUtils.shouldStoreEvent;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

public final class ServiceEventHelper {

    private static final int EVENT_PRIORITY = 100;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final HashMap<Conversation, MessagePriority> mMessagePriorityMap;

    private final HashMap<Conversation, Event> mEventMap;

    private final Server mServer;

    private Conversation mConversation;

    private MessagePriority mMessagePriority;

    public ServiceEventHelper(final Server server) {
        mServer = server;
        mMessagePriorityMap = new HashMap<>();
        mEventMap = new HashMap<>();

        getBus().registerSticky(new Object() {
            @Subscribe
            public void onConversationChanged(final OnConversationChanged conversationChanged) {
                mConversation = conversationChanged.conversation;
            }
        });

        server.getServerEventBus().register(this, EVENT_PRIORITY);
    }

    public void unregister() {
        mServer.getServerEventBus().unregister(this);
    }

    public void clearMessagePriority() {
        mMessagePriority = null;
    }

    public void clearMessagePriority(final Conversation conversation) {
        mMessagePriorityMap.remove(conversation);
    }

    public MessagePriority getSubMessagePriority(final Conversation title) {
        return mMessagePriorityMap.get(title);
    }

    public Event getSubEvent(final Conversation id) {
        return mEventMap.get(id);
    }

    public MessagePriority getMessagePriority() {
        return mMessagePriority;
    }

    private void setMessagePriority(final MessagePriority priority) {
        if (mMessagePriority == null || mMessagePriority.compareTo(priority) < 0) {
            mMessagePriority = priority;
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final NewPrivateMessage event) {
        final QueryUser user = mServer.getUserChannelInterface().getQueryUser(
                event.nick);
        onIRCEvent(MessagePriority.HIGH, user, getLastStorableEvent(user.getBuffer()));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final JoinEvent event) {
        final Channel channel = mServer.getUserChannelInterface().getChannel(event.channelName);
        onIRCEvent(MessagePriority.LOW, channel, getLastStorableEvent(channel.getBuffer()));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final ChannelEvent event) {
        final Conversation conversation = event.channel;
        if (shouldStoreEvent(event)) {
            // TODO - fix this horrible code
            if (event instanceof ChannelWorldUserEvent) {
                final ChannelWorldUserEvent userEvent = (ChannelWorldUserEvent) event;

                if (userEvent.userMentioned) {
                    onIRCEvent(MessagePriority.HIGH, conversation, event);

                    // Forward the event UI side
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getBus().post(new OnChannelMentionEvent(mServer,
                                    (Channel) conversation));
                        }
                    });
                } else if (event.getClass().equals(ChannelWorldMessageEvent.class)
                        || event.getClass().equals(ChannelWorldActionEvent.class)) {
                    onIRCEvent(MessagePriority.MEDIUM, conversation, event);
                } else {
                    onIRCEvent(MessagePriority.LOW, conversation, event);
                }
            } else {
                onIRCEvent(MessagePriority.LOW, conversation, event);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final QueryEvent event) {
        onIRCEvent(MessagePriority.HIGH, event.user, event);
    }

    private void setSubMessagePriority(final Conversation conversation,
            final MessagePriority priority) {
        final MessagePriority oldPriority = mMessagePriorityMap.get(conversation);
        if (oldPriority == null || oldPriority.compareTo(priority) < 0) {
            mMessagePriorityMap.put(conversation, priority);
        }
    }

    private void setSubEvent(final Conversation title, final Event event) {
        mEventMap.put(title, event);
    }

    private void onIRCEvent(final MessagePriority priority, final Conversation conversation,
            final Event event) {
        if (conversation.equals(mConversation)) {
            if (!conversation.equals(conversation.getServer())) {
                setSubEvent(conversation, event);
            }
        } else {
            if (conversation.equals(conversation.getServer())) {
                setMessagePriority(priority);
            } else {
                setSubMessagePriority(conversation, priority);
                setSubEvent(conversation, event);
            }
        }
    }
}