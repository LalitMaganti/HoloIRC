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

package com.fusionx.ircinterface.connection;

import android.content.Context;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.Utils;

import java.util.HashMap;
import java.util.Iterator;

public class ConnectionManager extends HashMap<String, ConnectionWrapper> {
    private static final long serialVersionUID = 2426166268063489300L;
    private final Context applicationContext;

    public ConnectionManager(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void disconnectAll() {
        final Iterator<ConnectionWrapper> iterator = values().iterator();
        while (iterator.hasNext()) {
            final ConnectionWrapper wrapper = iterator.next();
            if (wrapper.getServer().getStatus().equals(applicationContext
                    .getString(R.string.status_connected))) {
                wrapper.getServer().getWriter().quitServer(Utils.getQuitReason(applicationContext));
            } else if (wrapper.isAlive()) {
                wrapper.interrupt();
            }
            iterator.remove();
        }
    }
}