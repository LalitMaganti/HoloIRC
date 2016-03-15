package com.fusionx.lightirc.ui;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.util.EventUtils;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.event.Event;

public class IRCAdapter<T extends Event> extends RecyclerView.Adapter<IRCAdapter.IRCViewHolder>
        implements Filterable {

    private final Object mLock = new Object();

    private final Context mContext;

    private final LayoutInflater mInflater;

    private boolean mShouldFilter;

    private IRCFilter mFilter;

    private List<T> mObjects;

    private EventCache mEventCache;

    public IRCAdapter(final Context context, final EventCache cache, final boolean filter) {
        mContext = context;
        mObjects = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
        mEventCache = cache;
        mShouldFilter = filter;
    }

    @Override
    public IRCViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = initView(parent);
        return new IRCViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IRCViewHolder holder, final int position) {
        final Event event = getEvent(position);
        addTimestampIfRequired(holder, event);
        if (mEventCache == null) {
            // This should only happen when the fragment is about to be removed anyway so it
            // doesn't matter that we are displaying invalid data
            return;
        }
        holder.message.setText(mEventCache.get(event).getMessage());

        Linkify.addLinks(holder.message, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
    }

    @Override
    public int getItemCount() {
        synchronized (mLock) {
            return mObjects.size();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(final T event) {
        if (mShouldFilter && !EventUtils.shouldStoreEvent(event)) {
            return;
        }

        synchronized (mLock) {
            final int size = mObjects.size();
            mObjects.add(event);
            notifyItemInserted(size);
        }
    }

    public void setData(final List<? extends T> list, final Runnable runnable) {
        if (mShouldFilter) {
            getFilter().setDataToFilter(list);
            getFilter().setCallback(runnable);
            getFilter().filter(null);
        } else {
            synchronized (mLock) {
                mObjects = new ArrayList<>(list);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public IRCFilter getFilter() {
        if (mFilter == null) {
            mFilter = new IRCFilter();
        }
        return mFilter;
    }

    public Event getEvent(final int position) {
        synchronized (mLock) {
            return mObjects.get(position);
        }
    }

    private View initView(final ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.irc_listview_textview, parent, false);

        final IRCViewHolder holder = new IRCViewHolder(view);

        UIUtils.setRobotoLight(mContext, holder.timestamp);
        holder.timestamp.setTextSize(AppPreferences.getAppPreferences().getMainFontSize());

        UIUtils.setRobotoLight(mContext, holder.message);
        holder.message.setTextSize(AppPreferences.getAppPreferences().getMainFontSize());

        return view;
    }

    private void addTimestampIfRequired(IRCViewHolder holder, final Event event) {
        if (AppPreferences.getAppPreferences().shouldDisplayTimestamps()) {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(event.timestamp.format("%H:%M"));
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }
    }

    public static class IRCViewHolder extends RecyclerView.ViewHolder {

        public final TextView timestamp;

        public final TextView message;

        public IRCViewHolder(final View itemView) {
            super(itemView);

            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            message = (TextView) itemView.findViewById(R.id.message);
        }
    }

    private class IRCFilter extends Filter {

        private List<T> mDataToFilter = new ArrayList<>();

        private Runnable mCallback;

        public void setDataToFilter(final List<? extends T> list) {
            mDataToFilter = ImmutableList.copyOf(list);
        }

        public void setCallback(final Runnable callback) {
            mCallback = callback;
        }

        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final List<T> resultList = FluentIterable.from(mDataToFilter)
                    .filter(EventUtils::shouldStoreEvent)
                    .copyInto(new ArrayList<>());
            final FilterResults results = new FilterResults();
            results.values = resultList;
            results.count = resultList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            mObjects = (List<T>) results.values;
            notifyDataSetChanged();
            if (mCallback != null) {
                mCallback.run();
            }
        }
    }
}