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

import co.fusionx.relay.event.server.InviteEvent;

class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.InviteViewHolder> {

    private final LayoutInflater mLayoutInflater;

    private final List<InviteEvent> mInviteEvents;

    private final View.OnClickListener mAcceptListener;

    private final View.OnClickListener mDeclineListener;

    public InviteAdapter(final Context context,
            final View.OnClickListener acceptListener,
            final View.OnClickListener declineListener) {
        mLayoutInflater = LayoutInflater.from(context);

        mInviteEvents = new ArrayList<>();
        mAcceptListener = acceptListener;
        mDeclineListener = declineListener;
    }

    @Override
    public InviteViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mLayoutInflater.inflate(R.layout.invite_list_item, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final InviteViewHolder holder, final int position) {
        final InviteEvent event = getItem(position);

        holder.acceptButton.setOnClickListener(mAcceptListener);
        holder.acceptButton.setTag(event);

        holder.declineButton.setOnClickListener(mDeclineListener);
        holder.declineButton.setTag(event);

        holder.textView.setText(getItem(position).channelName);
    }

    public void setItems(Collection<InviteEvent> items) {
        mInviteEvents.clear();
        if (items != null) {
            mInviteEvents.addAll(items);
        }
        notifyDataSetChanged();
    }

    public InviteEvent getItem(final int position) {
        return mInviteEvents.get(position);
    }

    @Override
    public int getItemCount() {
        return mInviteEvents.size();
    }

    public void remove(final InviteEvent event) {
        final int index = mInviteEvents.indexOf(event);
        mInviteEvents.remove(index);
        notifyItemRemoved(index);
    }

    protected static class InviteViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        private final ImageView acceptButton;

        private final ImageView declineButton;

        public InviteViewHolder(final View itemView) {
            super(itemView);

            acceptButton = (ImageView) itemView.findViewById(R.id.accept_list_item);
            declineButton = (ImageView) itemView.findViewById(R.id.decline_list_item);

            textView = (TextView) itemView.findViewById(R.id.invite_list_item_title);
        }
    }
}
