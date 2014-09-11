package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.misc.ExpandableRecyclerViewAdapter;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.model.MessagePriority;
import com.fusionx.lightirc.model.SessionContainer;
import com.fusionx.lightirc.service.IRCService;
import com.fusionx.lightirc.service.ServiceEventInterceptor;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.fusionx.relay.conversation.Conversation;
import co.fusionx.relay.core.SessionStatus;
import co.fusionx.relay.event.Event;

public class SessionOverviewAdapter extends
        ExpandableRecyclerViewAdapter<SessionOverviewAdapter.ServerGroupViewHolder,
                SessionOverviewAdapter.SessionChildViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private final List<SessionContainer> mContainers;

    private final Context mContext;

    private final View.OnClickListener mGroupListener;

    private final View.OnClickListener mChildListener;

    public SessionOverviewAdapter(final Context context, final View.OnClickListener groupListener,
            final View.OnClickListener childListener) {
        mContext = context;
        mGroupListener = groupListener;
        mChildListener = childListener;

        mContainers = new ArrayList<>();

        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ServerGroupViewHolder onCreateGroupViewHolder(final ViewGroup parent) {
        final View view = mLayoutInflater.inflate(R.layout.main_list_group, parent, false);
        return new ServerGroupViewHolder(view, p -> view.post(() -> notifyItemChanged(p)));
    }

    @Override
    public SessionChildViewHolder onCreateChildViewHolder(final ViewGroup parent) {
        final View view = mLayoutInflater.inflate(R.layout.main_list_child, parent, false);
        return new SessionChildViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(final ServerGroupViewHolder holder,
            final int groupPosition) {
        final SessionContainer container = mContainers.get(groupPosition);
        final ServiceEventInterceptor helper = IRCService.getEventHelper(container.getSession());
        holder.itemView.setOnClickListener(mGroupListener);

        final TextView title = (TextView) holder.itemView.findViewById(R.id.server_title);
        final SpannableStringBuilder builder = new SpannableStringBuilder(container.getTitle());
        if (helper != null) {
            final MessagePriority priority = helper.getMessagePriority();
            if (priority != null) {
                builder.setSpan(UIUtils.getSpanFromPriority(priority), 0,
                        container.getTitle().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        title.setText(builder);

        final TextView status = ((TextView) holder.itemView.findViewById(R.id.server_status));
        status.setText(MiscUtils.getStatusString(mContext, container.getSession() == null
                ? SessionStatus.DISCONNECTED
                : container.getSession().getStatus()));

        final View divider = holder.itemView.findViewById(R.id.divider);
        final ImageView expandButton = (ImageView) holder.itemView.findViewById(R.id.button_expand);

        final boolean isExpanded = isGroupExpanded(groupPosition);
        if (container.getConversationCount() == 0) {
            expandButton.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            expandButton.setVisibility(View.VISIBLE);
            divider.setVisibility(isExpanded ? View.INVISIBLE : View.VISIBLE);
            expandButton.setTag(!isExpanded);

            final int drawableId = UIUtils.resolveResourceIdFromAttr(mContext, isExpanded
                    ? R.attr.expand_close_menu_drawable : R.attr.expand_open_menu_drawable);
            expandButton.setImageDrawable(mContext.getResources().getDrawable(drawableId));
        }
    }

    @Override
    public void onBindChildViewHolder(final SessionChildViewHolder holder,
            final int groupPosition, final int childPosition) {
        final SessionContainer container = mContainers.get(groupPosition);
        final Conversation conversation = container.getConversation(childPosition);

        holder.itemView.setOnClickListener(mChildListener);

        final ServiceEventInterceptor helper = IRCService.getEventHelper(container.getSession());
        final EventCache cache = IRCService.getEventCache(container.getSession());

        final TextView textView = (TextView) holder.itemView.findViewById(R.id.child_title);

        final MessagePriority priority = helper.getSubMessagePriority(conversation);
        if (priority == null) {
            textView.setText(conversation.getId());
        } else {
            final CharacterStyle span = UIUtils.getSpanFromPriority(priority);
            final SpannableStringBuilder builder = new SpannableStringBuilder(conversation.getId());
            builder.setSpan(span, 0, conversation.getId().length(), 0);
            textView.setText(builder);
        }

        final Event event = helper.getSubEvent(conversation);
        final TextView textEvent = (TextView) holder.itemView.findViewById(R.id.child_event);

        // Event might be null when the channel is first being established
        if (event == null) {
            textEvent.setText(R.string.no_event);
        } else {
            final EventDecorator decorator = cache.get(event);
            textEvent.setText(decorator.getMessage());
        }

        final boolean isLastChild = childPosition == container.getConversationCount() - 1;
        final View divider = holder.itemView.findViewById(R.id.divider);
        divider.setVisibility(isLastChild ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getGroupCount() {
        return mContainers.size();
    }

    @Override
    public int getChildCount(final int groupPos) {
        return mContainers.get(groupPos).getConversationCount();
    }

    public void addAll(final Collection<? extends SessionContainer> collection) {
        final int size = mContainers.size();
        mContainers.addAll(collection);
        notifyItemRangeInserted(size, collection.size());
    }

    public void clear() {
        final int size = mContainers.size();
        mContainers.clear();
        notifyItemRangeRemoved(0, size);
    }

    public SessionContainer getGroup(final int groupPosition) {
        return mContainers.get(groupPosition);
    }

    public void addChild(final int groupPosition, final Conversation conversation) {
        final SessionContainer container = mContainers.get(groupPosition);
        container.addConversation(conversation);

        final int rawPosition = getRawPosition(groupPosition);
        if (isGroupExpanded(groupPosition)) {
            notifyItemInserted(rawPosition + container.getConversationCount());
        }
        notifyItemChanged(rawPosition);
    }

    public void removeChild(final int groupPosition, final Conversation conversation) {
        final SessionContainer container = mContainers.get(groupPosition);
        final int position = container.removeConversation(conversation);

        final int rawPosition = getRawPosition(groupPosition);
        if (isGroupExpanded(groupPosition)) {
            notifyItemRemoved(rawPosition + position);
        }
        notifyItemChanged(rawPosition);
    }

    public static class ServerGroupViewHolder
            extends ExpandableRecyclerViewAdapter.GroupViewHolder {

        public final ImageView expandButton;

        public ServerGroupViewHolder(final View itemView, final ExpandButtonListener listener) {
            super(itemView, itemView.findViewById(R.id.button_expand), listener);

            expandButton = (ImageView) itemView.findViewById(R.id.button_expand);
        }
    }

    public static class SessionChildViewHolder
            extends ExpandableRecyclerViewAdapter.ChildViewHolder {

        public SessionChildViewHolder(final View itemView) {
            super(itemView);
        }
    }
}