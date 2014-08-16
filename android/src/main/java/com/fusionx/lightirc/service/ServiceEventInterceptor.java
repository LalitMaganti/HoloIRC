package com.fusionx.lightirc.service;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.event.OnChannelMentionEvent;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnQueryEvent;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.model.MessagePriority;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.Map;
import java.util.Set;

import co.fusionx.relay.Conversation;
import co.fusionx.relay.Server;
import co.fusionx.relay.event.Event;
import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.event.channel.ChannelWorldActionEvent;
import co.fusionx.relay.event.channel.ChannelWorldMessageEvent;
import co.fusionx.relay.event.channel.ChannelWorldUserEvent;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.event.server.DCCChatRequestEvent;
import co.fusionx.relay.event.server.DCCFileRequestEvent;
import co.fusionx.relay.event.server.DCCRequestEvent;
import co.fusionx.relay.event.server.InviteEvent;
import co.fusionx.relay.event.server.JoinEvent;
import co.fusionx.relay.event.server.NewPrivateMessageEvent;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

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

    private Conversation mConversation;

    private MessagePriority mMessagePriority;

    public ServiceEventInterceptor(final Server server) {
        mServer = server;
        mMessagePriorityMap = new THashMap<>();
        mEventMap = new THashMap<>();
        mInviteEvents = new THashSet<>();
        mDCCRequests = new THashSet<>();

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
                    mHandler.post(() -> getBus().post(new OnChannelMentionEvent(event.channel)));
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
            mHandler.post(() -> getBus().post(new OnQueryEvent(event.user)));
        }
    }
    /*
     * Event interception ends here
     */

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final InviteEvent event) {
        mInviteEvents.add(event);
    }

    @Subscribe(threadType = ThreadType.MAIN)
    public void onEvent(final DCCRequestEvent event) {
        mDCCRequests.add(event);
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

    public Server getServer() {
        return mServer;
    }

    public void acceptDCCConnection(final DCCRequestEvent event) {
        mDCCRequests.remove(event);
        if (event instanceof DCCChatRequestEvent) {
            final DCCChatRequestEvent chat = (DCCChatRequestEvent) event;
            chat.getPendingConnection().acceptConnection();
        } else if (event instanceof DCCFileRequestEvent) {
            final DCCFileRequestEvent file = (DCCFileRequestEvent) event;
            final File output = new File(AppPreferences.getAppPreferences()
                    .getDCCDownloadDirectory(), file.getPendingConnection().getArgument());
            file.getPendingConnection().acceptConnection(output);
        }
    }

    public void declineDCCRequestEvent(final DCCRequestEvent event) {
        mDCCRequests.remove(event);
        event.pendingConnection.declineConnection();
    }
}