package com.fusionx.lightirc.service;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnDCCChatEvent;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.event.OnServerStatusChanged;
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

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.dcc.event.chat.DCCChatEvent;
import co.fusionx.relay.dcc.event.file.DCCFileGetStartedEvent;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.ChannelWorldActionEvent;
import co.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import co.fusionx.relay.event.channel.ChannelWorldUserEvent;
import co.fusionx.relay.event.query.QueryActionWorldEvent;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.event.query.QueryMessageWorldEvent;
import co.fusionx.relay.event.server.DCCChatRequestEvent;
import co.fusionx.relay.event.server.DCCRequestEvent;
import co.fusionx.relay.event.server.DCCSendRequestEvent;
import co.fusionx.relay.event.server.InviteEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;
import co.fusionx.relay.event.server.StatusChangeEvent;

import static com.fusionx.lightirc.util.EventUtils.getLastStorableEvent;
import static com.fusionx.lightirc.util.EventUtils.shouldStoreEvent;
import static com.fusionx.lightirc.util.MiscUtils.getBus;

/**
 * Intercepts IRC events which need some special action performed on
 */
public final class ServiceEventInterceptor {

    private static final int EVENT_PRIORITY = 100;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Server mServer;

    private final Map<Conversation, MessagePriority> mMessagePriorityMap;

    private final Map<Conversation, Event> mEventMap;

    private final Set<InviteEvent> mInviteEvents;

    private Set<DCCRequestEvent> mDCCRequests;

    private ConnectionStatus mLastStatus;

    private Conversation mConversation;

    private MessagePriority mMessagePriority;

    public ServiceEventInterceptor(final Server server) {
        mServer = server;
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

        server.getServerWideBus().register(this, EVENT_PRIORITY);
    }

    public void unregister() {
        mServer.getServerWideBus().unregister(this);
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

    public ConnectionStatus getLastKnownStatus() {
        return mLastStatus;
    }

    /*
     * Event interception start here
     */
    @Subscribe(threadType = ThreadType.MAIN)
    public void onPrivateMessage(final NewPrivateMessageEvent event) {
        onIRCEvent(MessagePriority.HIGH, event.user, getLastStorableEvent(event.user.getBuffer()));
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
            if (event instanceof ChannelWorldMessageEvent
                    || event instanceof ChannelWorldActionEvent) {
                final ChannelWorldUserEvent userEvent = (ChannelWorldUserEvent) event;
                if (userEvent.userMentioned) {
                    onIRCEvent(MessagePriority.HIGH, conversation, event);

                    // Forward the event UI side
                    String message = event instanceof ChannelWorldMessageEvent
                            ? ((ChannelWorldMessageEvent) event).message
                            : ((ChannelWorldActionEvent) event).action;
                    OnChannelMentionEvent uiEvent = new OnChannelMentionEvent(event.channel,
                            userEvent.userNick, message);
                    mHandler.post(() -> getBus().post(uiEvent));
                } else {
                    onIRCEvent(MessagePriority.MEDIUM, conversation, event);
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
            if (event instanceof QueryMessageWorldEvent
                    || event instanceof QueryActionWorldEvent) {
                onIRCEvent(MessagePriority.HIGH, event.user, event);

                final String message = event instanceof QueryMessageWorldEvent
                        ? ((QueryMessageWorldEvent) event).message
                        : ((QueryActionWorldEvent) event).action;

                // Forward the event UI side
                mHandler.post(() -> getBus().post(new OnQueryEvent(event.user, message)));
            } else {
                onIRCEvent(MessagePriority.MEDIUM, event.user, event);
            }
        }
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final StatusChangeEvent event) {
        ConnectionStatus newStatus = event.server.getStatus();
        if (mLastStatus != newStatus) {
            // forward the event UI side
            mHandler.post(() -> getBus().post(new OnServerStatusChanged(event.server, newStatus)));
        }
        mLastStatus = newStatus;
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
        if (conversation.equals(conversation.getServer())) {
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

    public Server getServer() {
        return mServer;
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
            mServer.sendJoin(event.channelName);
        }
        mInviteEvents.removeAll(inviteEvents);
    }

    public void declineInviteEvents(final Collection<InviteEvent> inviteEvents) {
        mInviteEvents.removeAll(inviteEvents);
    }
}
