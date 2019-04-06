package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.misc.Theme;
import com.fusionx.lightirc.model.EventDecorator;
import com.fusionx.lightirc.model.MessagePriority;
import com.fusionx.lightirc.model.ServerConversationContainer;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.base.Server;
import co.fusionx.relay.event.Event;

import static com.fusionx.lightirc.util.UIUtils.getSpanFromPriority;

public class ExpandableServerListAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater mInflater;

    private final Context mContext;

    private final List<ServerConversationContainer> mServerListItems;

    private final IRCService mIRCService;

    private ExpandableListView mListView;

    public ExpandableServerListAdapter(final Context context, final ArrayList<ServerConversationContainer>
            builders, final ExpandableListView listView, final IRCService service) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mServerListItems = builders;
        mListView = listView;
        mIRCService = service;
    }

    @Override
    public int getGroupCount() {
        return mServerListItems.size();
    }

    @Override
    public int getChildrenCount(final int groupPos) {
        return mServerListItems.get(groupPos).getConversationCount();
    }

    @Override
    public ServerConversationContainer getGroup(final int groupPos) {
        return mServerListItems.get(groupPos);
    }

    @Override
    public Conversation getChild(final int groupPos, final int childPos) {
        return mServerListItems.get(groupPos).getConversation(childPos);
    }

    @Override
    public long getGroupId(final int groupPos) {
        return groupPos;
    }

    @Override
    public long getChildId(final int groupPos, final int childPos) {
        return childPos;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // TODO - ViewHolder pattern
    @Override
    public View getGroupView(final int groupPos, final boolean isExpanded, View convertView,
            final ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_list_group, parent, false);
        }

        final ServerConversationContainer listItem = getGroup(groupPos);
        final ServiceEventInterceptor helper = mIRCService.getEventHelper(listItem.getServer());

        final TextView title = (TextView) convertView.findViewById(R.id.server_title);
        final SpannableStringBuilder builder = new SpannableStringBuilder(listItem.getTitle());
        if (helper != null) {
            final MessagePriority priority = helper.getMessagePriority();
            if (priority != null) {
                builder.setSpan(getSpanFromPriority(priority), 0, listItem.getTitle().length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        title.setText(builder);

        final TextView status = ((TextView) convertView.findViewById(R.id.server_status));
        status.setText(MiscUtils.getStatusString(mContext, listItem.getServer() != null
                ? listItem.getServer().getStatus()
                : ConnectionStatus.DISCONNECTED));

        final View divider = convertView.findViewById(R.id.divider);
        final ImageView expandButton = (ImageView) convertView.findViewById(R.id.button_expand);
        if (listItem.getConversationCount() == 0) {
            expandButton.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            expandButton.setVisibility(View.VISIBLE);
            divider.setVisibility(isExpanded ? View.INVISIBLE : View.VISIBLE);
            expandButton.setTag(!isExpanded);

            final int drawableId = UIUtils.resolveResourceIdFromAttr(mContext, isExpanded
                    ? R.attr.expand_close_menu_drawable : R.attr.expand_open_menu_drawable);
            expandButton.setImageDrawable(mContext.getResources().getDrawable(drawableId));
            expandButton.setOnClickListener(new ExpandListener(groupPos));
        }
        return convertView;
    }

    // TODO - ViewHolder pattern
    @Override
    public View getChildView(final int groupPos, final int childPos, final boolean isLastChild,
            View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_list_child, parent, false);
        }

        final ServerConversationContainer listItem = getGroup(groupPos);
        final Conversation conversation = getChild(groupPos, childPos);

        final ServiceEventInterceptor helper = mIRCService.getEventHelper(listItem.getServer());
        final EventCache cache = IRCService.getEventCache(listItem.getServer(),
                AppPreferences.getAppPreferences().getTheme() == Theme.DARK);

        final TextView textView = (TextView) convertView.findViewById(R.id.child_title);

        final MessagePriority priority = helper.getSubMessagePriority(conversation);
        if (priority == null) {
            textView.setText(conversation.getId());
        } else {
            final CharacterStyle span = getSpanFromPriority(priority);
            final SpannableStringBuilder builder = new SpannableStringBuilder(conversation.getId());
            builder.setSpan(span, 0, conversation.getId().length(), 0);
            textView.setText(builder);
        }

        final Event event = helper.getSubEvent(conversation);
        final TextView textEvent = (TextView) convertView.findViewById(R.id.child_event);

        // Event might be null when the channel is first being established
        if (event == null) {
            textEvent.setText(R.string.no_event);
        } else {
            final EventDecorator decorator = cache.get(event);
            textEvent.setText(decorator.getMessage());
        }

        final View divider = convertView.findViewById(R.id.divider);
        divider.setVisibility(isLastChild ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void refreshConversations() {
        for (final ServerConversationContainer serverConversationContainer : mServerListItems) {
            serverConversationContainer.refreshConversations();
        }
    }

    public void removeServer(final Server server) {
        for (final ServerConversationContainer serverConversationContainer : mServerListItems) {
            if (!server.equals(serverConversationContainer.getServer())) {
                continue;
            }
            serverConversationContainer.setServer(null);
            serverConversationContainer.removeAll();
        }
        notifyDataSetChanged();
    }

    public final class ExpandListener implements View.OnClickListener {

        private final int mGroupPos;

        private ExpandListener(final int group) {
            mGroupPos = group;
        }

        @Override
        public void onClick(final View v) {
            final boolean collapsed = (boolean) v.getTag();
            final int drawableId;

            if (collapsed) {
                drawableId = UIUtils.resolveResourceIdFromAttr(mContext,
                        R.attr.expand_close_menu_drawable);
                mListView.expandGroup(mGroupPos);
            } else {
                drawableId = UIUtils.resolveResourceIdFromAttr(mContext,
                        R.attr.expand_open_menu_drawable);
                mListView.collapseGroup(mGroupPos);
            }

            final ImageView imageView = (ImageView) v;
            imageView.setImageDrawable(mContext.getResources().getDrawable(drawableId));
            v.setTag(!collapsed);
        }
    }
}