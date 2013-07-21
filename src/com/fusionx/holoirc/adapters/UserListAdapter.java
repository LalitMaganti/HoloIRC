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

package com.fusionx.holoirc.adapters;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.fusionx.holoirc.R;
import com.fusionx.holoirc.irc.IRCUserComparator;
import com.fusionx.lightlibrary.adapters.SelectionAdapter;

import java.util.ArrayList;

public class UserListAdapter extends SelectionAdapter<String> {
    public UserListAdapter(final Context context, final ArrayList<String> array) {
        super(context, array);
    }

    public void sort() {
        super.sort(new IRCUserComparator());
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(Html.fromHtml(getItem(position)));
        return view;
    }
}