package com.fusionx.lightirc.service;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnDCCChatEvent;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.model.MessagePriority;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.IRCConnection;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.dcc.event.chat.DCCChatEvent;
import co.fusionx.relay.dcc.event.file.DCCFileGetStartedEvent;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.ChannelWorldActionEvent;
import co.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import co.fusionx.relay.event.channel.ChannelWorldUserEvent;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.event.server.DCCChatRequestEvent;
import co.fusionx.relay.event.server.DCCRequestEvent;
import co.fusionx.relay.event.server.DCCSendRequestEvent;
import co.fusionx.relay.event.server.InviteEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;

import static com.fusionx.lightirc.util.EventUtils.getLastStorableEvent;
import static com.fusionx.lightirc.util.EventUtils.shouldStoreEvent;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

/**
 * Intercepts IRC events which need some special action performed on
 */
public final class ServiceEventInterceptor {

    private static final int EVENT_PRIORITY = 100;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final IRCConnection mIRCConnection;

    private final Map<Conversation, MessagePriority> mMessagePriorityMap;

    private final Map<Conversation, Event> mEventMap;

    private final Set<InviteEvent> mInviteEvents;

    private Set<DCCRequestEvent> mDCCRequests;

    private Conversation mConversation;

    private MessagePriority mMessagePriority;

    public ServiceEventInterceptor(final IRCConnection connection) {
        mIRCConnection = connection;
        mMessagePriorityMap = new HashMap<>();
        mEventMap = new HashMap<>();
        mInviteEvents = new HashSet<>();
        mDCCRequests = new HashSet<>();

        getBus().registerSticky(new Object() {
            @Subscribe
            public void onConversationChanged(final OnConversationChanged conversationChanged) {
                mConversation = conversationChanged.conversation;
            }
        });

        connection.getSuperBus().register(this, EVENT_PRIORITY);
    }

    public void unregister() {
        mIRCConnection.getSuperBus().unregister(this);
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

    public void clearMessagePriority() {
        mMessagePriority = null;
    }

    public void clearMessagePriority(final Conversation conversation) {
        mMessagePriorityMap.remove(conversation);
    }

    public Set<InviteEvent> getInviteEvents() {
        return mInviteEvents;
    }

    public Set<DCCRequestEvent> getDCCRequests() {
        return mDCCRequests;
    }

    /*
     * Event interception start here
     */
    @Subscribe(threadType = ThreadType.MAIN)
    public void onPrivateMessage(final NewPrivateMessageEvent event) {
        onIRCEvent(MessagePriority.HIGH, event.user, getLastStorableEvent(event.user.getBuffer()));

        // Forward the event UI side
        mHandler.post(() -> getBus().post(new OnQueryEvent(mIRCConnection, event.user)));
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final JoinEvent event) {
        onIRCEvent(MessagePriority.LOW, event.channel,
                getLastStorableEvent(event.channel.getBuffer()));
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final ChannelEvent event) {
        if (shouldStoreEvent(event)) {
            // TODO - fix this horrible code
            final Conversation conversation = event.channel;
            if (event instanceof ChannelWorldUserEvent) {
                final ChannelWorldUserEvent userEvent = (ChannelWorldUserEvent) event;

                if (userEvent.userMentioned) {
                    onIRCEvent(MessagePriority.HIGH, conversation, event);

                    // Forward the event UI side
                    mHandler.post(() -> getBus().post(new OnChannelMentionEvent(mIRCConnection,
                            event.channel)));
                } else if (event.getClass().equals(ChannelWorldMessageEvent.class)
                        || event.getClass().equals(ChannelWorldActionEvent.class)) {
                    onIRCEvent(MessagePriority.MEDIUM, conversation, event);
                } else {
                    onIRCEvent(MessagePriority.LOW, conversation, event);
                }
            } else if (conversation == null) {
                // Either a part or a kick
                // TODO
            } else {
                onIRCEvent(MessagePriority.LOW, conversation, event);
            }
        }
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final QueryEvent event) {
        if (shouldStoreEvent(event)) {
            onIRCEvent(MessagePriority.HIGH, event.user, event);

            // Forward the event UI side
            mHandler.post(() -> getBus().post(new OnQueryEvent(mIRCConnection, event.user)));
        }
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final InviteEvent event) {
        mInviteEvents.add(event);
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final DCCRequestEvent event) {
        mDCCRequests.add(event);
    }

    // DCC Events
    @Subscribe(threadType = ThreadType.MAIN)
    public void onChatEvent(final DCCChatEvent event) {
        onIRCEvent(MessagePriority.HIGH, event.chatConversation, event);

        // Forward the event UI side
        mHandler.post(() -> getBus().post(new OnDCCChatEvent(event.chatConversation)));
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onChatEvent(final DCCFileGetStartedEvent event) {
        onIRCEvent(MessagePriority.HIGH, event.fileConversation,
                getLastStorableEvent(event.fileConversation.getBuffer()));
    }
    /*
     * Event interception ends here
     */

    private void onIRCEvent(final MessagePriority priority, final Conversation conversation,
            final Event event) {
        if (conversation instanceof Server) {
            if (!conversation.equals(mConversation)) {
                setMessagePriority(priority);
            }
        } else {
            if (!conversation.equals(mConversation)) {
                setConversationPriority(conversation, priority);
            }
            setConversationEvent(conversation, event);
        }
    }

    private void setConversationPriority(final Conversation conversation,
            final MessagePriority priority) {
        final MessagePriority oldPriority = mMessagePriorityMap.get(conversation);
        if (oldPriority == null || oldPriority.compareTo(priority) < 0) {
            mMessagePriorityMap.put(conversation, priority);
        }
    }

    private void setConversationEvent(final Conversation conversation, final Event event) {
        mEventMap.put(conversation, event);
    }

    public IRCConnection getIRCConnection() {
        return mIRCConnection;
    }

    public void acceptDCCConnection(final DCCRequestEvent event) {
        mDCCRequests.remove(event);

        if (event instanceof DCCChatRequestEvent) {
            final DCCChatRequestEvent chat = (DCCChatRequestEvent) event;
            chat.getPendingConnection().acceptConnection();
        } else if (event instanceof DCCSendRequestEvent) {
            final DCCSendRequestEvent file = (DCCSendRequestEvent) event;
            final File output = new File(AppPreferences.getAppPreferences()
                    .getDCCDownloadDirectory(), file.getPendingConnection().getArgument());
            file.getPendingConnection().acceptConnection(output);
        }
    }

    public void declineDCCRequestEvent(final DCCRequestEvent event) {
        mDCCRequests.remove(event);
        event.pendingConnection.declineConnection();
    }

    public void acceptInviteEvents(final Collection<InviteEvent> inviteEvents) {
        for (final InviteEvent event : inviteEvents) {
            mIRCConnection.getServer().sendJoin(event.channelName);
        }
        mInviteEvents.removeAll(inviteEvents);
    }

    public void declineInviteEvents(final Collection<InviteEvent> inviteEvents) {
        mInviteEvents.removeAll(inviteEvents);
    }
}