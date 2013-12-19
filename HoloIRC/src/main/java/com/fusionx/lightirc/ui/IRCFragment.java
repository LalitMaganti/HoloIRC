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

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCAnimationAdapter;
import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.relay.Message;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public abstract class IRCFragment extends ListFragment implements TextView.OnEditorActionListener {

    String mTitle = null;

    EditText mMessageBox = null;

    IRCMessageAdapter mMessageAdapter;

    boolean mCachingImportant = true;

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflate.inflate(R.layout.fragment_irc, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupListAdapter();
        getListView().setSelection(getListView().getCount());
    }

    void setupListAdapter() {
        final List<Message> messages = onRetrieveMessages();
        if (messages != null) {
            mMessageAdapter = new IRCMessageAdapter(getActivity(), messages);
            final IRCAnimationAdapter adapter = new IRCAnimationAdapter(mMessageAdapter);
            adapter.setAbsListView(getListView());
            setListAdapter(adapter);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessageBox = (EditText) getView().findViewById(R.id.fragment_irc_message_box);
        mMessageBox.setOnEditorActionListener(this);
        mTitle = getArguments().getString("title");
    }

    public final void disableEditText() {
        mMessageBox.setEnabled(false);
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final CharSequence text = mMessageBox.getText();
        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo
                .IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event
                .getKeyCode() == KeyEvent.KEYCODE_ENTER) && StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            mMessageBox.setText("");
            onSendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public void setCachingImportant(boolean cachingImportant) {
        mCachingImportant = cachingImportant;
    }

    // Abstract methods
    protected abstract void onSendMessage(final String message);

    protected abstract List<Message> onRetrieveMessages();

    public abstract FragmentTypeEnum getType();

    // Getters and setters
    public String getTitle() {
        return mTitle;
    }
}