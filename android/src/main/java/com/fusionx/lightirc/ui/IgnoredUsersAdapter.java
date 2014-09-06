package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IgnoredUsersAdapter
        extends RecyclerView.Adapter<IgnoredUsersAdapter.IgnoreViewHolder> {

    private final Context mContext;

    private final LayoutInflater mLayoutInflater;

    private final List<String> mIgnoredUsers;

    public IgnoredUsersAdapter(final Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        mIgnoredUsers = new ArrayList<>();
    }

    @Override
    public IgnoreViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mLayoutInflater.inflate(R.layout.default_listview_textview, parent,
                false);
        return new IgnoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IgnoreViewHolder holder, final int position) {
        final String user = mIgnoredUsers.get(position);
        holder.textView.setText(user);
    }

    @Override
    public int getItemCount() {
        return mIgnoredUsers.size();
    }

    public void addAll(final Collection<String> arrayList) {
        final int size = mIgnoredUsers.size();
        mIgnoredUsers.addAll(arrayList);
        notifyItemRangeInserted(size, arrayList.size());
    }

    public void add(final String input) {
        final int size = mIgnoredUsers.size();
        mIgnoredUsers.add(input);
        notifyItemInserted(size);
    }

    public List<String> getItems() {
        return new ArrayList<>(mIgnoredUsers);
    }

    public static class IgnoreViewHolder extends RecyclerView.ViewHolder {

        public final TextView textView;

        public IgnoreViewHolder(final View itemView) {
            super(itemView);

            textView = (TextView) itemView;
        }
    }
}