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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.ui.widget.ServerCardInterface;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerStatus;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ServerListAdapter extends BaseCollectionAdapter<ServerCardInterface> {

    private final Callbacks mCallbacks;

    public ServerListAdapter(final Activity activity, final List<ServerCardInterface> list) {
        super(activity, R.layout.item_server_card, list);

        try {
            mCallbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "ServerListAdapter.Callbacks");
        }
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final ServerCardInterface card = getItem(position);
        final Server server = card.getTitle() == null ? null :
                mCallbacks.getServer(card.getTitle());
        return getItem(position).getView(convertView, parent, server);
    }

    public ArrayList<String> getListOfTitles(final ServerCardInterface exclusion) {
        final ArrayList<String> listOfTitles = new ArrayList<>();
        for (ServerCardInterface builder : mObjects) {
            if (!builder.equals(exclusion) && builder.getTitle() != null) {
                listOfTitles.add(builder.getTitle());
            }
        }
        return listOfTitles;
    }

    public int getNumberOfConnectedServers() {
        int i = 0;
        for (final ServerCardInterface builder : mObjects) {
            final Server server = mCallbacks.getServer(builder.getTitle());
            if (server != null && server.getStatus() == ServerStatus.CONNECTED) {
                i += 1;
            }
        }
        return i;
    }

    public interface Callbacks {

        public Server getServer(final String title);
    }
}