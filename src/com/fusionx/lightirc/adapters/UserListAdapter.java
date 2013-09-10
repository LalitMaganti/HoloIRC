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
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.SynchronizedCollection;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.util.UIUtils;

import lombok.Setter;

public class UserListAdapter extends BaseCollectionAdapter<ChannelUser> implements
        StickyListHeadersAdapter {
    @Setter
    private Channel channel;

    public UserListAdapter(Context context, SynchronizedCollection<ChannelUser> objects) {
        super(context, R.layout.default_listview_textview, objects);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTypeface(UIUtils.getRobotoLight(getContext()));
        view.setText(getItem(position).getSpannableNick(channel));
        return view;
    }

    @Override
    public View getHeaderView(int i, View convertView, ViewGroup viewGroup) {
        final TextView view = (TextView) ((convertView != null) ? convertView : LayoutInflater.from
                (getContext()).inflate(R.layout.sliding_menu_header, viewGroup, false));
        final char firstChar = getFirstCharacter(i);
        if (firstChar == '@') {
            view.setText(channel.getNumberOfOwners() + " owners");
        } else if (firstChar == '+') {
            view.setText(channel.getNumberOfVoices() + " voices");
        } else {
            view.setText(channel.getNumberOfNormalUsers() + " users");
        }
        return view;
    }

    @Override
    public long getHeaderId(int i) {
        return getFirstCharacter(i);
    }

    char getFirstCharacter(final int position) {
        final ChannelUser user = getItem(position);
        return user.getUserPrefix(channel);
    }

    public void setInternalSet(SynchronizedCollection<ChannelUser> set) {
        synchronized (mObjects.getLock()) {
            mObjects = set;
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }
}