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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import com.fusionx.lightirc.R;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class IRCFragment extends Fragment implements TextView.OnEditorActionListener {
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    private String title = null;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private TextView textView = null;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    private EditText editText = null;

    String serverName = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflate,
                             final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflate.inflate(R.layout.fragment_irc, container, false);

        setTextView((TextView) rootView.findViewById(R.id.textview));
        setEditText((EditText) rootView.findViewById(R.id.editText1));

        getEditText().setOnEditorActionListener(this);

        setTitle(getArguments().getString("title"));

        if (getArguments().getString("serverName") != null) {
            serverName = getArguments().getString("serverName");
        } else {
            serverName = getTitle();
        }

        return rootView;
    }

    public void appendToTextView(final String text) {
        getTextView().append(Html.fromHtml(text.replace("\n", "<br/>")));
        Linkify.addLinks(getTextView(), Linkify.ALL);
    }

    void writeToTextView(final String text) {
        getTextView().setText(Html.fromHtml(text.replace("\n", "<br/>")));
        Linkify.addLinks(getTextView(), Linkify.ALL);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.scrollview);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void partOrCloseIRC(final boolean channel) {
        throw new NullPointerException();
    }
}