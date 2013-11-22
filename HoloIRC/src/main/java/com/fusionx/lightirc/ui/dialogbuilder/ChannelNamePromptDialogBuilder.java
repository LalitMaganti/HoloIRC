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

package com.fusionx.lightirc.ui.dialogbuilder;

import android.content.Context;

import com.fusionx.lightirc.R;

public abstract class ChannelNamePromptDialogBuilder extends PromptDialogBuilder {
    public ChannelNamePromptDialogBuilder(final Context context) {
        super(context, context.getString(R.string.prompt_dialog_channel_name), context.getString(R.string.prompt_dialog_including_starting), "");
    }

    public ChannelNamePromptDialogBuilder(final Context context, final String channelName) {
        super(context, context.getString(R.string.prompt_dialog_channel_name), context.getString(R.string.prompt_dialog_including_starting), channelName);
    }
}
