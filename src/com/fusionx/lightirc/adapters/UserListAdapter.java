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
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fusionx.irc.ChannelUser;

import java.util.SortedSet;

import lombok.Setter;

public class UserListAdapter extends SelectionAdapter<ChannelUser> {
    @Setter
    private String channelName;

    public UserListAdapter(final Context context, final SortedSet<ChannelUser> array) {
        super(context, array);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText(Html.fromHtml(getItem(position).getPrettyNick(channelName)));
        return view;
    }
}