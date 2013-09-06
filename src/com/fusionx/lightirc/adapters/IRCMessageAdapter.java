package com.fusionx.lightirc.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fusionx.lightirc.util.UIUtils;

import java.util.List;

public class IRCMessageAdapter extends ArrayAdapter<String> {
    public IRCMessageAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTypeface(UIUtils.getRobotoLight(view.getContext()));
        view.setText(Html.fromHtml(getItem(position)));
        Linkify.addLinks(view, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        if(UIUtils.hasHoneycomb()) {
            view.setTextIsSelectable(true);
        }
        return view;
    }
}