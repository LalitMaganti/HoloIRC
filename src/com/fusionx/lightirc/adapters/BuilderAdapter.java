/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fusionx.irc.Server;
import com.fusionx.irc.ServerConfiguration;
import com.fusionx.lightirc.R;

public class BuilderAdapter extends ArrayAdapter<ServerConfiguration.Builder> {
    private final Context mContext;
    private final BuilderAdapterListenerInterface mListener;

    public BuilderAdapter(final Context context) {
        super(context, android.R.layout.simple_list_item_1);
        mContext = context;

        try {
            mListener = (BuilderAdapterListenerInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BuilderAdapterListenerInterface");
        }
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            final LayoutInflater vi = LayoutInflater.from(mContext);
            view = vi.inflate(R.layout.item_server_card, parent, false);
        }

        final TextView textView = (TextView) view.findViewById(R.id.title);
        final TextView description = (TextView) view.findViewById(R.id.description);
        if (textView != null) {
            textView.setText(getItem(position).getTitle());
        }
        if (description != null) {
            final Server bot = mListener.getBot(getItem(position).getTitle());
            if (bot != null) {
                description.setText(bot.getStatus());
            } else {
                description.setText(mContext.getString(R.string.status_disconnected));
            }
        }

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.overflow_menu);
        linearLayout.setTag(getItem(position));

        linearLayout = (LinearLayout) view.findViewById(R.id.contentLayout);
        linearLayout.setTag(getItem(position));

        return view;
    }

    public interface BuilderAdapterListenerInterface {
        public Server getBot(final String title);
    }
}