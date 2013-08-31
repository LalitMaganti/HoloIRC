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

import com.fusionx.lightirc.R;

import java.util.ArrayList;
import java.util.Arrays;

public class UserChannelActionsAdapter extends ActionsArrayAdapter {
    public UserChannelActionsAdapter(Context context) {
        super(context, new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array
                .channel_actions))));
    }

    public void setServerVisible() {
        mList.clear();
    }

    public void setChannelVisible(boolean visible) {
        mList.clear();
        mList.addAll(visible ? Arrays.asList(mContext.getResources().getStringArray(R.array
                .channel_actions)) : Arrays.asList(mContext.getResources().getStringArray(R.array
                .user_actions)));
    }
}
