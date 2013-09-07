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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.Constants;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.ServerConfiguration;
import com.github.espiandev.showcaseview.ShowcaseView;

import java.util.ArrayList;

public class ServerListAdapter extends ArrayAdapter<ServerConfiguration.Builder> {
    private final Activity mActivity;
    private final BuilderAdapterCallback mCallback;

    public ServerListAdapter(final Activity activity, ArrayList<ServerConfiguration.Builder> list) {
        super(activity, android.R.layout.simple_list_item_1, list);
        mActivity = activity;

        try {
            mCallback = (BuilderAdapterCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "BuilderAdapterCallback");
        }
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view;
        final ServerConfiguration.Builder builder = getItem(position);
        if (convertView == null) {
            final LayoutInflater vi = LayoutInflater.from(mActivity);
            view = vi.inflate(R.layout.item_server_card, parent, false);
        } else {
            view = convertView;
        }

        final TextView textView = (TextView) view.findViewById(R.id.title);
        final TextView description = (TextView) view.findViewById(R.id.description);
        if (textView != null) {
            textView.setText(builder.getTitle());
        }
        if (description != null) {
            final Server server = mCallback.getServer(builder.getTitle());
            if (server != null) {
                description.setText(server.getStatus());
            } else {
                description.setText(mActivity.getString(R.string.status_disconnected));
            }
        }

        final LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.contentLayout);
        contentLayout.setTag(builder);

        final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.overflow_menu);
        linearLayout.setTag(builder);

        return view;
    }

    public interface BuilderAdapterCallback {
        public Server getServer(final String title);
    }
}