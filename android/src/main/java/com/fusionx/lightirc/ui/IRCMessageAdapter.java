package com.fusionx.lightirc.ui;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.util.EventUtils;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.fusionx.relay.event.Event;

public class IRCMessageAdapter<T extends Event> extends BaseAdapter implements Filterable {

    private final Object mLock = new Object();

    private final Context mContext;

    private final LayoutInflater mInflater;

    private boolean mShouldFilter;

    private IRCFilter mFilter;

    private List<T> mObjects;

    private EventCache mEventCache;

    public IRCMessageAdapter(final Context context, final EventCache cache, final boolean filter) {
        mContext = context;
        mObjects = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
        mEventCache = cache;
        mShouldFilter = filter;
    }

    @Override
    public int getCount() {
        synchronized (mLock) {
            return mObjects.size();
        }
    }

    @Override
    public Object getItem(final int position) {
        return getEvent(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = convertView == null ? initView(parent) : convertView;
        final ViewHolder holder = (ViewHolder) view.getTag();

        final Event event = getEvent(position);
        addTimestampIfRequired(holder, event);
        holder.message.setText(mEventCache.get(event).getMessage());

        Linkify.addLinks(holder.message, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        return view;
    }

    public void add(final T event) {
        if (mShouldFilter && !EventUtils.shouldStoreEvent(event)) {
            return;
        }

        synchronized (mLock) {
            mObjects.add(event);
        }
        notifyDataSetChanged();
    }

    public void setData(final List<T> list, final Runnable runnable) {
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

    private Event getEvent(final int position) {
        synchronized (mLock) {
            return mObjects.get(position);
        }
    }

    private View initView(final ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.irc_listview_textview, parent, false);

        final TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
        UIUtils.setRobotoLight(mContext, timestamp);
        timestamp.setTextSize(AppPreferences.getAppPreferences().getMainFontSize());

        final TextView message = (TextView) view.findViewById(R.id.message);
        UIUtils.setRobotoLight(mContext, message);
        message.setTextSize(AppPreferences.getAppPreferences().getMainFontSize());

        final ViewHolder holder = new ViewHolder(timestamp, message);
        view.setTag(holder);

        return view;
    }

    private void addTimestampIfRequired(ViewHolder holder, final Event event) {
        if (AppPreferences.getAppPreferences().shouldDisplayTimestamps()) {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(event.timestamp.format("%H:%M"));
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }
    }

    private static class ViewHolder {

        public final TextView timestamp;

        public final TextView message;

        private ViewHolder(final TextView timestamp, final TextView message) {
            this.timestamp = timestamp;
            this.message = message;
        }
    }

    private class IRCFilter extends Filter {

        private List<T> mDataToFilter = new ArrayList<>();

        private Runnable mCallback;

        public void setDataToFilter(final List<T> list) {
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