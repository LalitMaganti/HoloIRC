package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.model.WrappedServerListItem;
import com.fusionx.lightirc.util.MiscUtils;
import com.fusionx.relay.ServerStatus;
import com.fusionx.relay.interfaces.SubServerObject;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ExpandableServerListAdapter extends BaseExpandableListAdapter {

    private final Callback mCallback;

    private final LayoutInflater mInflater;

    private final Context mContext;

    private final List<WrappedServerListItem> mServerListItems;

    private ExpandableListView mListView;

    public ExpandableServerListAdapter(final Context context, final List<WrappedServerListItem>
            builders, final ExpandableListView listView, final Callback callback) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mServerListItems = builders;
        mListView = listView;
        mCallback = callback;
    }

    @Override
    public SubServerObject getChild(final int groupPos, final int childPos) {
        return mServerListItems.get(groupPos).getServerObjects().get(childPos);
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
        return mServerListItems.get(groupPos).getServerObjects().size();
    }

    // TODO - ViewHolder pattern
    @Override
    public View getChildView(final int groupPos, final int childPos, final boolean isLastChild,
            View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_list_child, parent, false);
        }

        final List<SubServerObject> list = mServerListItems.get(groupPos).getServerObjects();
        final TextView textView = (TextView) convertView.findViewById(R.id.server_title);
        textView.setText(list.get(childPos).getId());

        final View divider = convertView.findViewById(R.id.divider);
        divider.setVisibility(isLastChild ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public WrappedServerListItem getGroup(final int groupPos) {
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

        final WrappedServerListItem listItem = mServerListItems.get(groupPos);

        final TextView title = (TextView) convertView.findViewById(R.id.server_title);
        title.setText(listItem.getTitle());
        final TextView status = ((TextView) convertView.findViewById(R.id.server_status));
        status.setText(MiscUtils.getStatusString(mContext, listItem.getServer() != null
                ? listItem.getServer().getStatus()
                : ServerStatus.DISCONNECTED));

        final View divider = convertView.findViewById(R.id.divider);
        final ImageView expandButton = (ImageView) convertView.findViewById(R.id.button_expand);
        if (listItem.getServerObjects().size() == 0) {
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

        final ImageView overflow = (ImageView) convertView.findViewById(R.id.overflow_menu);
        overflow.setOnClickListener(new OverflowListener(groupPos));
        overflow.setTag(listItem);

        return convertView;
    }

    public void setListView(ExpandableListView listView) {
        mListView = listView;
    }

    public interface Callback {

        public void onEditServer(final WrappedServerListItem builder);
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

    public final class OverflowListener implements View.OnClickListener,
            PopupMenu.OnMenuItemClickListener {

        private final int mGroupPos;

        private OverflowListener(final int group) {
            mGroupPos = group;
        }

        @Override
        public boolean onMenuItemClick(final MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.activity_server_list_popup_edit:
                    mCallback.onEditServer(mServerListItems.get(mGroupPos));
                    break;
                case R.id.activity_server_list_popup_disconnect:
                    //mCallback.disconnectFromServer(this);
                    break;
                case R.id.activity_server_list_popup_delete:
                    //mCallback.deleteServer(this);
                    break;
                default:
                    return false;
            }
            return true;
        }

        @Override
        public void onClick(final View v) {
            final WrappedServerListItem listItem = (WrappedServerListItem) v.getTag();

            final PopupMenu popup = new PopupMenu(mContext, v);
            popup.inflate(R.menu.activity_server_list_popup);

            if (listItem.getServer() == null) {
                popup.getMenu().getItem(0).setEnabled(false);
            } else {
                popup.getMenu().getItem(1).setEnabled(false);
                popup.getMenu().getItem(2).setEnabled(false);
            }

            popup.setOnMenuItemClickListener(this);
            popup.show();
        }
    }
}