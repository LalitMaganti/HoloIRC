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

import java.util.ArrayList;

public class ChannelFragment extends IRCFragment {
    private String serverName;

    public ArrayList<String> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    private ArrayList<String> userList;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_irc, container, false);

        setTitle(getArguments().getString("channel"));
        serverName = getArguments().getString("serverName");

        final String buffer = getArguments().getString("buffer");
        if (buffer != null) {
            writeToTextView(buffer, rootView);
        }

        final EditText edittext = (EditText) rootView.findViewById(R.id.editText1);

        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final EditText edittext = (EditText) rootView.findViewById(R.id.editText1);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ((ServerChannelActivity) getActivity())
                            .channelMessageToParse(serverName, getTitle(), edittext.getText().toString());

                    edittext.getText().clear();
                }
                return false;
            }
        });


        return rootView;
    }
}
