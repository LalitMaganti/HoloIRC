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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.fusionx.lightirc.misc.Utils;
import lombok.AccessLevel;
import lombok.Setter;

public class ActionsArrayAdapter extends ArrayAdapter<String> {
    @Setter(AccessLevel.PUBLIC)
    private boolean connected;

    private final LayoutInflater inflate;
    private final Context applicationContext;

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) inflate.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        row.setText(getItem(position));

        if (!isEnabled(position)) {
            row.setTextColor(Color.GRAY);
        } else if (Utils.themeIsHoloLight(applicationContext)) {
            row.setTextColor(Color.BLACK);
        } else {
            row.setTextColor(Color.WHITE);
        }

        return row;
    }

    public ActionsArrayAdapter(final Context context, final CharSequence[] objects) {
        super(context, android.R.layout.simple_list_item_1, (String[]) objects);
        inflate = LayoutInflater.from(context);
        applicationContext = context.getApplicationContext();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !((position == 0) || (position == 1)) || connected;
    }
}
