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

import lombok.Getter;

public class ServerActionsAdapter extends ActionsArrayAdapter {
    @Getter
    private boolean connected = false;

    public ServerActionsAdapter(final Context context, final String[] objects) {
        super(context, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        return !((position == 0) || (position == 1)) || connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        mObjects.set(2, connected ? "Disconnect" : "Close");
    }
}