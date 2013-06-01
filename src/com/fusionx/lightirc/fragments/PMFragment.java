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

package com.fusionx.lightirc.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.activity.ServerChannelActivity;

public class PMFragment extends IRCFragment implements TextView.OnEditorActionListener {
    private String serverName;
    private EditText edittext;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_irc, container, false);

        serverName = getArguments().getString("serverName");

        final String buffer = getArguments().getString("buffer");
        if (buffer != null) {
            writeToTextView(buffer, rootView);
        }

        setTitle(getArguments().getString("nick"));

        edittext = (EditText) rootView.findViewById(R.id.editText1);

        edittext.setOnEditorActionListener(this);

        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            final String message = edittext.getText().toString();
            edittext.setText("");

            final ParserTask task = new ParserTask();
            String[] strings = {serverName, getTitle(), message};
            task.doInBackground(strings);
        }
        return false;
    }

    private class ParserTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(final String... strings) {
            if (strings != null) {
                final String server = strings[0];
                final String userNick = strings[1];
                final String message = strings[2];
                ((ServerChannelActivity) getActivity())
                        .parser.userMessageToParse(server, userNick, message);
            }
            return null;
        }
    }
}
