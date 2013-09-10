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

package com.fusionx.lightirc.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.FragmentTypeEnum;

import org.apache.commons.lang3.StringUtils;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;

import lombok.Getter;

public abstract class IRCFragment extends ListFragment implements TextView
        .OnEditorActionListener {
    @Getter
    protected String title = null;
    protected EditText mEditText = null;

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflate.inflate(R.layout.fragment_irc, container, false);
        mEditText = (EditText) rootView.findViewById(R.id.editText1);
        mEditText.setOnEditorActionListener(this);
        title = getArguments().getString("title");
        return rootView;
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
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) &&
                StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            mEditText.setText("");
            sendMessage(message);
            return true;
        }
        return false;
    }

    @Override
    public BaseAdapter getListAdapter() {
        return (BaseAdapter) super.getListAdapter();
    }

    public abstract void sendMessage(final String message);

    public abstract FragmentTypeEnum getType();
}