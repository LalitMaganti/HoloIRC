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

package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.NickCache;
import com.fusionx.lightirc.util.UIUtils;
import com.fusionx.relay.Channel;
import com.fusionx.relay.ChannelUser;
import com.fusionx.relay.constants.UserLevel;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;
import java.util.TreeSet;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class UserListAdapter extends BaseCollectionAdapter<ChannelUser> implements
        StickyListHeadersAdapter {

    private final LayoutInflater mInflater;

    private Channel mChannel;

    public UserListAdapter(Context context, Set<ChannelUser> objects) {
        super(context, R.layout.default_listview_textview, objects);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) mInflater.inflate(R.layout.default_listview_textview, parent, false);
            UIUtils.setRobotoLight(getContext(), view);
        }
        final ChannelUser user = getItem(position);
        final char prefix = user.getChannelPrivileges(mChannel).getPrefix();
        final SpannableStringBuilder builder = new SpannableStringBuilder(
                prefix + user.getNick().getNickAsString());
        final ForegroundColorSpan span = new ForegroundColorSpan(
                NickCache.getNickCache().get(user.getNick()).getColour());
        builder.setSpan(span, 0, builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        view.setText(builder);
        return view;
    }

    @Override
    public View getHeaderView(int i, View convertView, ViewGroup viewGroup) {
        TextView view = (TextView) convertView;
        if (convertView == null) {
            view = (TextView) mInflater.inflate(R.layout.sliding_menu_header, viewGroup, false);
        }

        final UserLevel levelEnum = getItem(i).getChannelPrivileges(mChannel);
        view.setText(mChannel.getNumberOfUsersType(levelEnum) + " " + levelEnum.getName());
        return view;
    }

    @Override
    public long getHeaderId(int position) {
        final ChannelUser user = getItem(position);
        return user.getChannelPrivileges(mChannel).getPrefix();
    }

    public void setInternalSet(final TreeSet<ChannelUser> set) {
        synchronized (mLock) {
            mObjects = set;
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void setChannel(final Channel channel) {
        mChannel = channel;
    }
}