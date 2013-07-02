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

public class ServerFragment extends IRCFragment {
    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE && getEditText().getText() != null) {
            final String message = getEditText().getText().toString();
            getEditText().setText("");

            final ParserTask task = new ParserTask();
            String[] strings = {getTitle(), message};
            task.execute(strings);
        }
        return false;
    }

    private class ParserTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(final String... strings) {
            if (strings != null) {
                final String server = strings[0];
                final String message = strings[1];
                ((IRCFragmentActivity) getActivity())
                        .getParser().serverMessageToParse(server, message);
            }
            return null;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        final String buffer = ((IRCFragmentActivity) getActivity()).getService().getBot(serverName).getBuffer();
        writeToTextView(buffer);
    }
}
