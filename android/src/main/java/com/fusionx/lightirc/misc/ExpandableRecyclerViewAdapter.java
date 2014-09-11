package com.fusionx.lightirc.misc;

import com.fusionx.lightirc.model.SessionContainer;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

public abstract class ExpandableRecyclerViewAdapter<G extends ExpandableRecyclerViewAdapter
        .GroupViewHolder, C extends ExpandableRecyclerViewAdapter.ChildViewHolder>
        extends RecyclerView.Adapter<ExpandableRecyclerViewAdapter.ViewHolder> {

    public static final int VIEW_TYPE_GROUP = 0;

    public static final int VIEW_TYPE_CHILD = 1;

    private final SparseBooleanArray mExpandedGroups = new SparseBooleanArray();

    public boolean isGroupExpanded(final int groupPosition) {
        return mExpandedGroups.get(groupPosition);
    }

    public void collapseGroup(final int groupPosition) {
        mExpandedGroups.delete(groupPosition);
        final int rawPosition = getRawPosition(groupPosition);
        notifyItemRangeRemoved(rawPosition + 1, getChildCount(groupPosition));
    }

    public void expandGroup(final int groupPosition) {
        mExpandedGroups.put(groupPosition, true);
        final int rawPosition = getRawPosition(groupPosition);
        notifyItemRangeInserted(rawPosition + 1, getChildCount(groupPosition));
    }

    public int getRawPosition(final int groupPosition) {
        int position = 0;
        for (int i = 0; i < groupPosition; i++) {
            position++;
            if (isGroupExpanded(i)) {
                position += getChildCount(groupPosition);
            }
        }
        return position;
    }

    public void toggleGroup(final int groupPosition) {
        if (isGroupExpanded(groupPosition)) {
            collapseGroup(groupPosition);
        } else {
            expandGroup(groupPosition);
        }
    }

    public boolean isGroup(final int rawPosition) {
        int position = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            if (rawPosition == position) {
                return true;
            }
            position++;

            if (isGroupExpanded(i)) {
                position += getChildCount(i);
                if (rawPosition < position) {
                    return false;
                }
            }
        }
        return false;
    }

    public int getGroupPosition(final int rawPosition) {
        int position = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            if (rawPosition == position) {
                return i;
            }
            position++;

            if (isGroupExpanded(i)) {
                position += getChildCount(i);
                if (rawPosition < position) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getChildPosition(final int rawPosition, final int groupPos) {
        int position = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            position++;

            if (isGroupExpanded(i)) {
                if (i == groupPos) {
                    return rawPosition - position;
                } else {
                    position += getChildCount(i);
                }
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < getGroupCount(); i++) {
            count += 1;
            if (isGroupExpanded(i)) {
                count += getChildCount(i);
            }
        }
        return count;
    }

    @Override
    public final ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == VIEW_TYPE_GROUP) {
            return onCreateGroupViewHolder(parent);
        } else if (viewType == VIEW_TYPE_CHILD) {
            return onCreateChildViewHolder(parent);
        }
        // This case is invalid
        return null;
    }

    @Override
    public final void onBindViewHolder(final ViewHolder holder, final int position) {
        int groupPosition = getGroupPosition(position);
        if (isGroup(position)) {
            final G groupViewHolder = (G) holder;
            groupViewHolder.expandView.setOnClickListener(v -> {
                toggleGroup(groupPosition);

                groupViewHolder.expandListener.onExpandButtonClicked(position);
            });
            onBindGroupViewHolder(groupViewHolder, groupPosition);
        } else {
            int childPosition = getChildPosition(position, groupPosition);
            final C childViewHolder = (C) holder;
            onBindChildViewHolder(childViewHolder, groupPosition, childPosition);
        }
    }

    public void removeGroup(final int groupPosition) {
        final int rawPosition = getRawPosition(groupPosition);
        final boolean groupExpanded = isGroupExpanded(groupPosition);

        mExpandedGroups.delete(groupPosition);
        if (groupExpanded) {
            notifyItemRangeRemoved(rawPosition, getChildCount(groupPosition) + 1);
        } else {
            notifyItemRemoved(rawPosition);
        }
    }

    public void notifyChildChanged(final int groupPosition, final int childPosition) {
        if (isGroupExpanded(groupPosition)) {
            notifyItemChanged(getRawPosition(groupPosition) + childPosition + 1);
        }
    }

    @Override
    public final int getItemViewType(final int position) {
        return isGroup(position) ? VIEW_TYPE_GROUP : VIEW_TYPE_CHILD;
    }

    public abstract G onCreateGroupViewHolder(final ViewGroup parent);

    public abstract C onCreateChildViewHolder(final ViewGroup parent);

    public abstract void onBindGroupViewHolder(final G groupViewHolder,
            final int groupPosition);

    public abstract void onBindChildViewHolder(final C childViewHolder,
            final int groupPosition, final int childPosition);

    public abstract int getGroupCount();

    public abstract int getChildCount(final int groupPos);

    public static interface ExpandButtonListener {

        public void onExpandButtonClicked(final int groupPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(final View itemView) {
            super(itemView);
        }
    }

    public static class GroupViewHolder extends ViewHolder {

        public final View expandView;

        public final ExpandButtonListener expandListener;

        public GroupViewHolder(final View itemView, final View expandView,
                final ExpandButtonListener expandListener) {
            super(itemView);

            this.expandView = expandView;
            this.expandListener = expandListener;
        }
    }

    public static class ChildViewHolder extends ViewHolder {

        public ChildViewHolder(final View itemView) {
            super(itemView);
        }
    }
}