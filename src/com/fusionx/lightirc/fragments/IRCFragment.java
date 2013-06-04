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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.fusionx.lightirc.R;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class IRCFragment extends Fragment  implements TextView.OnEditorActionListener,
        View.OnFocusChangeListener {
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    private String title;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    private TextView textView;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PROTECTED)
    private TextView editText;

    private InputMethodManager imm;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_irc, container, false);

        imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);

        setTextView((TextView) rootView.findViewById(R.id.textview));
        setEditText((EditText) rootView.findViewById(R.id.editText1));

        getEditText().setOnEditorActionListener(this);

        String buffer = getArguments().getString("buffer");
        if (buffer != null) {
            writeToTextView(buffer);
        }

        editText.setOnFocusChangeListener(this);
        return rootView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.equals(editText)) {
            if(hasFocus) {
                editText.post(new Runnable() {
                    @Override
                    public void run() {
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            } else {
                editText.post(new Runnable() {
                    @Override
                    public void run() {
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                });
            }
        }
    }

    public void writeToTextView(final String text) {
        textView.append(Html.fromHtml(text.replace("\n", "<br/>")));
    }
}