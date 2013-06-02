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

package com.fusionx.lightirc.cardsui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.fima.cardsui.objects.Card;
import com.fusionx.lightirc.R;
import org.pircbotx.Configuration;

public class ServerCard extends Card {
    private final Configuration.Builder mBuilder;
    private final String mStatus;

    public ServerCard(String titlePlay, String description, final Configuration.Builder builder) {
        super(titlePlay, description, "#fffffff", "#000000", true, true);
        mStatus = description;
        mBuilder = builder;
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_server_card, null);
        ((TextView) view.findViewById(R.id.title)).setText(titlePlay);
        ((TextView) view.findViewById(R.id.description)).setText(mStatus);
        return view;
    }

    @Override
    public View getView(final Context context) {
        View v = super.getView(context);
        View view = v.findViewById(R.id.contentLayout);
        view.setTag(mBuilder);
        View view2 = v.findViewById(R.id.overflow_menu);
        view2.setTag(mBuilder);
        //view.setLongClickable(true);
        //view.setOnLongClickListener((MainServerListActivity) context);
        return v;
    }

    @Override
    public View getViewFirst(final Context context) {
        View v = super.getView(context);
        View view = v.findViewById(R.id.contentLayout);
        view.setTag(mBuilder);
        View view2 = v.findViewById(R.id.overflow_menu);
        view2.setTag(mBuilder);
        //view.setLongClickable(true);
        //view.setOnLongClickListener((MainServerListActivity) context);
        return v;
    }

    @Override
    public View getViewLast(final Context context) {
        View v = super.getView(context);
        View view = v.findViewById(R.id.contentLayout);
        view.setTag(mBuilder);
        View view2 = v.findViewById(R.id.overflow_menu);
        view2.setTag(mBuilder);
        //view.setLongClickable(true);
        //view.setOnLongClickListener((MainServerListActivity) context);
        return v;
    }
}