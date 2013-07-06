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

package com.fusionx.lightirc.fragments.ircfragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.fusionx.lightirc.activity.IRCFragmentActivity;
import org.pircbotx.Channel;

public class ChannelFragment extends IRCFragment {
    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        final CharSequence text = getEditText().getText();
        if (i == EditorInfo.IME_ACTION_DONE && text != null && !text.equals("")) {
            final String message = text.toString();
            getEditText().setText("");

            final ParserTask task = new ParserTask();
            final String[] strings = {serverName, getTitle(), message};
            task.execute(strings);
        }
        return false;
    }

    private class ParserTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(final String... strings) {
            if (strings != null) {
                final String server = strings[0];
                final String channelName = strings[1];
                final String message = strings[2];
                ((IRCFragmentActivity) getActivity())
                        .getParser().channelMessageToParse(server, channelName, message);
            }
            return null;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        final Channel channel = ((IRCFragmentActivity) getActivity()).getService().getBot(serverName)
                .getUserChannelDao().getChannel(getTitle());
        writeToTextView(channel.getBuffer());
    }
}