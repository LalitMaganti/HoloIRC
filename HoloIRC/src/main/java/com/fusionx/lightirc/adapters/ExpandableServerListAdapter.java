package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.model.ServerWrapper;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.interfaces.Conversation;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ExpandableServerListAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater mInflater;

    private final Context mContext;

    private final List<ServerWrapper> mServerListItems;

    private ExpandableListView mListView;

    public ExpandableServerListAdapter(final Context context, final List<ServerWrapper>
            builders, final ExpandableListView listView) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mServerListItems = builders;
        mListView = listView;
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
    public View getChildView(final int groupPos, final int childPos, final boolean isLastChild,
            View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_list_child, parent, false);
        }

        final TextView textView = (TextView) convertView.findViewById(R.id.server_title);
        textView.setText(getChild(groupPos, childPos).getId());

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

    // TODO - ViewHolder pattern
    @Override
    public View getGroupView(final int groupPos, final boolean isExpanded, View convertView,
            final ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_list_group, parent, false);
        }

        final ServerWrapper listItem = getGroup(groupPos);

        final TextView title = (TextView) convertView.findViewById(R.id.server_title);
        title.setText(listItem.getTitle());
        final TextView status = ((TextView) convertView.findViewById(R.id.server_status));
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