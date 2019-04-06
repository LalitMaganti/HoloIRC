package app.holoirc.ui;

import app.holoirc.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IgnoredUsersAdapter
        extends RecyclerView.Adapter<IgnoredUsersAdapter.IgnoreViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private final List<String> mIgnoredUsers;

    private final View.OnClickListener mDeclineListener;

    public IgnoredUsersAdapter(final Context context, final View.OnClickListener declineListener) {
        mLayoutInflater = LayoutInflater.from(context);
        mDeclineListener = declineListener;

        mIgnoredUsers = new ArrayList<>();
    }

    @Override
    public IgnoreViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mLayoutInflater.inflate(R.layout.ignore_list_item, parent, false);
        return new IgnoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IgnoreViewHolder holder, final int position) {
        final String user = mIgnoredUsers.get(position);
        holder.titleView.setText(user);

        holder.declineView.setOnClickListener(mDeclineListener);
        holder.declineView.setTag(user);
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
        if (mIgnoredUsers.indexOf(input) == -1) {
            final int size = mIgnoredUsers.size();
            mIgnoredUsers.add(input);
            notifyItemInserted(size);
        }
    }

    public void remove(final String string) {
        final int index = mIgnoredUsers.indexOf(string);
        mIgnoredUsers.remove(index);
        notifyItemRemoved(index);
    }

    public List<String> getItems() {
        return new ArrayList<>(mIgnoredUsers);
    }

    public static class IgnoreViewHolder extends RecyclerView.ViewHolder {

        public final TextView titleView;

        private final ImageView declineView;

        public IgnoreViewHolder(final View itemView) {
            super(itemView);

            declineView = (ImageView) itemView.findViewById(R.id.decline_list_item);

            titleView = (TextView) itemView.findViewById(R.id.ignore_list_item_title);
        }
    }
}