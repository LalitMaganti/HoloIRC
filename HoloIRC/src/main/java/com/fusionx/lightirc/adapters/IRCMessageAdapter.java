package com.fusionx.lightirc.adapters;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.AppPreferences;
import com.fusionx.lightirc.util.MessageConversionUtils;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.event.Event;

import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IRCMessageAdapter<T extends Event> extends BaseAdapter {

    private final Object mLock = new Object();

    private final Context mContext;

    private final LayoutInflater mInflater;

    private final MessageConversionUtils mConverter;

    private List<T> mObjects;

    public IRCMessageAdapter(Context context) {
        mContext = context;
        mObjects = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
        mConverter = MessageConversionUtils.getConverter(mContext);
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

    private Event getEvent(final int position) {
        synchronized (mLock) {
            return mObjects.get(position);
        }
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
        setUpMessage(holder, event);

        Linkify.addLinks(holder.message, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        return view;
    }

    private View initView(final ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.irc_listview_textview, parent, false);

        final TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
        timestamp.setTypeface(UIUtils.getRobotoLight(mContext));

        final TextView message = (TextView) view.findViewById(R.id.message);
        message.setTypeface(UIUtils.getRobotoLight(mContext));

        final ViewHolder holder = new ViewHolder(timestamp, message);
        view.setTag(holder);

        return view;
    }

    private void setUpMessage(ViewHolder holder, Event event) {
        if (event.store == null) {
            mConverter.setEventMessage(event);
        }
        holder.message.setText((CharSequence) event.store);
    }

    private void addTimestampIfRequired(ViewHolder holder, final Event event) {
        if (AppPreferences.timestamp) {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(event.timestamp.format("%H:%M"));
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }
    }

    public void add(final T event) {
        synchronized (mLock) {
            mObjects.add(event);
        }
        notifyDataSetChanged();
    }

    public void setData(final List<T> list) {
        synchronized (mLock) {
            mObjects = list;
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
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
}