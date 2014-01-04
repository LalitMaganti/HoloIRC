package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.communication.MessageSpannedConverter;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.event.Event;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class IRCMessageAdapter<T extends Event> extends BaseAdapter {

    private final Object mLock = new Object();

    private final Context mContext;

    private List<T> mObjects;

    public IRCMessageAdapter(Context context, final List<T> objects) {
        mContext = context;
        mObjects = objects;
    }

    @Override
    public int getCount() {
        synchronized (mLock) {
            return mObjects.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return getEvent(position);
    }

    private Event getEvent(final int position) {
        synchronized (mLock) {
            return mObjects.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.irc_listview_textview, parent, false);
            final TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
            timestamp.setTypeface(UIUtils.getRobotoLight(mContext));

            final TextView message = (TextView) view.findViewById(R.id.message);
            message.setTypeface(UIUtils.getRobotoLight(mContext));

            holder = new ViewHolder(timestamp, message);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        final Event message = getEvent(position);
        if (AppPreferences.timestamp) {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(message.timestamp.format("%hh:Mm"));
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }
        if (message.store == null) {
            MessageSpannedConverter.getConverter(mContext).getEventMessage(message);
        }
        holder.message.setText((CharSequence) message.store);
        Linkify.addLinks(holder.message, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        return view;
    }

    public void add(final T event) {
        synchronized (mLock) {
            mObjects.add(event);
        }
        notifyDataSetChanged();
    }

    public void setInternalList(final List<T> list) {
        synchronized (mLock) {
            mObjects = list;
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {

        public final TextView timestamp;

        public final TextView message;

        private ViewHolder(final TextView timestamp, final TextView message) {
            this.timestamp = timestamp;
            this.message = message;
        }
    }
}