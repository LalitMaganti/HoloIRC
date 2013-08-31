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

package com.fusionx.lightirc.adapters.actions;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fusionx.common.Utils;
import com.fusionx.lightirc.R;

import java.util.List;

public abstract class ActionsArrayAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;
    protected Context mContext;
    protected List<String> mList;

    public ActionsArrayAdapter(final Context context, final List<String> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mList = objects;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) inflater.inflate(R.layout.default_listview_textview, parent, false);
        }
        Utils.setTypeface(getContext(), row);
        row.setText(getItem(position));

        if (!isEnabled(position)) {
            row.setTextColor(Color.GRAY);
        } else {
            row.setTextColor(Utils.getThemedTextColor(row.getContext()));
        }

        return row;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
}