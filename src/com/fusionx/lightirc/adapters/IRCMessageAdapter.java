package com.fusionx.lightirc.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Message;
import com.fusionx.lightirc.util.UIUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class IRCMessageAdapter extends ArrayAdapter<Message> {
    private Context activityContext;

    public IRCMessageAdapter(Context context, ArrayList<Message> objects) {
        super(context, R.layout.irc_listview_textview, objects);
        activityContext = context;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(activityContext).inflate(R.layout
                    .irc_listview_textview, parent, false);
            final TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
            timestamp.setTypeface(UIUtils.getRobotoLight(activityContext));
            final TextView message = (TextView) view.findViewById(R.id.message);
            message.setTypeface(UIUtils.getRobotoLight(activityContext));
            holder = new ViewHolder(timestamp, message);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        final Message message = getItem(position);
        if (StringUtils.isEmpty(message.timestamp)) {
            holder.timestamp.setVisibility(View.GONE);
        } else {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(message.timestamp);
        }
        holder.message.setText(message.message);
        Linkify.addLinks(holder.message, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        return view;
    }

    public void setActivityContext(final Context context) {
        activityContext = context;
    }

    private static class ViewHolder {
        public final TextView timestamp;
        public final TextView message;

        private ViewHolder(TextView timestamp, TextView message) {
            this.timestamp = timestamp;
            this.message = message;
        }
    }
}