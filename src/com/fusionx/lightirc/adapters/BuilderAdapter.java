/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.MainServerListActivity;
import com.fusionx.lightirc.service.IRCService;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class BuilderAdapter extends ArrayAdapter<Configuration.Builder> {
    private final MainServerListActivity mActivity;
    private final IRCService mService;

    public BuilderAdapter(final IRCService service, final MainServerListActivity activity) {
        super(activity, android.R.layout.simple_list_item_1);
        mService = service;
        mActivity = activity;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            final LayoutInflater vi = mActivity.getLayoutInflater();
            view = vi.inflate(R.layout.item_server_card, parent, false);
        }

        final TextView textView = (TextView) view.findViewById(R.id.title);
        final TextView description = (TextView) view.findViewById(R.id.description);
        if (textView != null) {
            textView.setText(getItem(position).getTitle());
        }
        if (description != null) {
            final PircBotX bot = mService.getBot(getItem(position).getTitle());
            if (bot != null) {
                description.setText(bot.getStatus());
            } else {
                description.setText(mActivity.getString(R.string.status_disconnected));
            }
        }

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.overflow_menu);
        linearLayout.setTag(getItem(position));

        linearLayout = (LinearLayout) view.findViewById(R.id.contentLayout);
        linearLayout.setTag(getItem(position));

        return view;
    }
}