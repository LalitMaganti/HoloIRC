package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.IRCService;
import com.fusionx.lightirc.communication.ServiceEventHelper;
import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.util.MessageConversionUtils;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.interfaces.Conversation;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpandableServerListAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater mInflater;

    private final Context mContext;

    private final ArrayList<ServerWrapper> mServerListItems;

    private final MessageConversionUtils mMessageConverter;

    private final IRCService mIRCService;

    private ExpandableListView mListView;

    public ExpandableServerListAdapter(final Context context, final ArrayList<ServerWrapper>
            builders, final ExpandableListView listView, final IRCService service) {
        mInflater = LayoutInflater.from(context);
        mMessageConverter = MessageConversionUtils.getConverter(context);
        mContext = context;
        mServerListItems = builders;
        mListView = listView;
        mIRCService = service;
    }

    @Override
    public Conversation getChild(final int groupPos, final int childPos) {
        return mServerListItems.get(groupPos).getSubServer(childPos);
    }

    @Override
    public long getChildId(final int groupPos, final int childPos) {
        return childPos;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getChildrenCount(final int groupPos) {
        return mServerListItems.get(groupPos).getSubServerSize();
    }

    // TODO - ViewHolder pattern
    @Override
    public View getGroupView(final int groupPos, final boolean isExpanded, View convertView,
            final ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_list_group, parent, false);
        }

        final ServerWrapper listItem = getGroup(groupPos);
        final ServiceEventHelper helper = mIRCService.getEventHelper(listItem.getTitle());

        final TextView title = (TextView) convertView.findViewById(R.id.child_title);
        final SpannableStringBuilder builder = new SpannableStringBuilder(listItem.getTitle());
        if (helper != null) {
            builder.setSpan(UIUtils.getSpanFromPriority(mContext, helper.getMessagePriority()), 0,
                    listItem.getTitle().length(), 0);
        }

        title.setText(listItem.getTitle());
        final TextView status = ((TextView) convertView.findViewById(R.id.child_event));
        status.setText(MiscUtils.getStatusString(mContext, listItem.getServer() != null
                ? listItem.getServer().getStatus()
                : ConnectionStatus.DISCONNECTED));

        final View divider = convertView.findViewById(R.id.divider);
        final ImageView expandButton = (ImageView) convertView.findViewById(R.id.button_expand);
        if (listItem.getSubServerSize() == 0) {
            expandButton.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            expandButton.setVisibility(View.VISIBLE);
            divider.setVisibility(isExpanded ? View.INVISIBLE : View.VISIBLE);
            expandButton.setTag(!isExpanded);

            final TypedValue typedvalueattr = new TypedValue();
            mContext.getTheme().resolveAttribute(isExpanded ? R.attr.expand_close_menu_drawable
                    : R.attr.expand_open_menu_drawable, typedvalueattr, true);
            expandButton.setImageDrawable(mContext.getResources().getDrawable(typedvalueattr
                    .resourceId));

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

        final ServerWrapper listItem = getGroup(groupPos);
        final Conversation conversation = getChild(groupPos, childPos);

        final ServiceEventHelper helper = mIRCService.getEventHelper(listItem.getTitle());

        final TextView textView = (TextView) convertView.findViewById(R.id.child_title);
        final SpannableStringBuilder builder = new SpannableStringBuilder(conversation.getId());
        builder.setSpan(UIUtils.getSpanFromPriority(mContext, helper.getSubMessagePriority
                (conversation.getId())), 0, conversation.getId().length(), 0);
        textView.setText(builder);

        final Event event = helper.getSubEvent(conversation.getId());
        final TextView textEvent = (TextView) convertView.findViewById(R.id.child_event);
        if (event.store == null) {
            mMessageConverter.setEventMessage(event);
        }
        textEvent.setText(event.store.toString());

        final View divider = convertView.findViewById(R.id.divider);
        divider.setVisibility(isLastChild ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public ServerWrapper getGroup(final int groupPos) {
        return mServerListItems.get(groupPos);
    }

    @Override
    public int getGroupCount() {
        return mServerListItems.size();
    }

    @Override
    public long getGroupId(final int groupPos) {
        return groupPos;
    }

    public final class ExpandListener implements View.OnClickListener {

        private final int mGroupPos;

        private ExpandListener(final int group) {
            mGroupPos = group;
        }

        @Override
        public void onClick(final View v) {
            final boolean collapsed = (boolean) v.getTag();
            final TypedValue typedvalueattr = new TypedValue();

            if (collapsed) {
                mContext.getTheme().resolveAttribute(R.attr
                        .expand_close_menu_drawable, typedvalueattr, true);
                mListView.expandGroup(mGroupPos);
            } else {
                mContext.getTheme().resolveAttribute(R.attr
                        .expand_open_menu_drawable, typedvalueattr, true);
                mListView.collapseGroup(mGroupPos);
            }

            final ImageView imageView = (ImageView) v;
            imageView.setImageDrawable(mContext.getResources().getDrawable(typedvalueattr
                    .resourceId));
            v.setTag(!collapsed);
        }
    }
}