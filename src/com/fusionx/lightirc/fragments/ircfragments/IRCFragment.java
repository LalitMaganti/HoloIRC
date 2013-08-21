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

package com.fusionx.lightirc.fragments.ircfragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.misc.FragmentType;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class IRCFragment extends Fragment implements TextView.OnEditorActionListener {
    @Getter
    protected String title = null;
    protected TextView mTextView = null;
    protected EditText mEditText = null;

    @Override
    public View onCreateView(final LayoutInflater inflate,
                             final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflate.inflate(R.layout.fragment_irc, container, false);

        mTextView = (TextView) rootView.findViewById(R.id.textview);
        mEditText = (EditText) rootView.findViewById(R.id.editText1);

        mEditText.setOnEditorActionListener(this);

        title = getArguments().getString("title");

        return rootView;
    }

    public void appendToTextView(final String text) {
        mTextView.append(Html.fromHtml(text.replace("\n", "<br/>")));
        Linkify.addLinks(mTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
    }

    void writeToTextView(final String text) {
        mTextView.setText(Html.fromHtml(text.replace("\n", "<br/>")));
        Linkify.addLinks(mTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
    }

    @Override
    public void onResume() {
        super.onResume();

        final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.scrollview);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void disableEditText() {
        mEditText.setEnabled(false);
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final CharSequence text = mEditText.getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            mEditText.setText("");

            sendMessage(message);
        }
        return false;
    }

    public abstract void sendMessage(final String message);

    public abstract FragmentType getType();

    public abstract Handler getHandler();
}