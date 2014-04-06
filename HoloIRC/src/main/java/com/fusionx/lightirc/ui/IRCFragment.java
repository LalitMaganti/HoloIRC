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
import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.relay.ConnectionStatus;
import com.fusionx.relay.event.Event;
import com.fusionx.relay.interfaces.Conversation;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public abstract class IRCFragment<T extends Event> extends ListFragment implements TextView
        .OnEditorActionListener {

    Conversation mConversation;

    @InjectView(R.id.fragment_irc_message_box)
    EditText mMessageBox;

    String mTitle;

    IRCMessageAdapter<T> mMessageAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
            final Bundle savedInstanceState) {
        return createView(container, inflate);
    }

    protected View createView(final ViewGroup container, final LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_irc, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final OnConversationChanged event = EventBus.getDefault().getStickyEvent
                (OnConversationChanged.class);
        mConversation = event.conversation;

        // Sets up the views
        ButterKnife.inject(this, view);

        mMessageBox.setOnEditorActionListener(this);
        mTitle = getArguments().getString("title");

        mMessageAdapter = new IRCMessageAdapter<>(getActivity());
        final AnimationAdapter adapter = new AlphaInAnimationAdapter(mMessageAdapter);
        adapter.setAbsListView(getListView());
        setListAdapter(adapter);

        if (savedInstanceState != null) {
            adapter.setShouldAnimateFromPosition(savedInstanceState.getInt("NUMBEROFITEMS"));
        }
        onResetBuffer();
        mConversation.getServer().getServerEventBus().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        getListView().setSelection(getListView().getCount());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("NUMBEROFITEMS", mMessageAdapter.getCount());
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
        }
        return false;
    }

    public void onResetBuffer() {
        if (mConversation.getServer().getStatus() == ConnectionStatus.CONNECTED) {
            mMessageAdapter.setData(new ArrayList<>(getAdapterData()));
        } else {
            mMessageAdapter.setData(new ArrayList<>(getDisconnectedAdapterData()));
        }
    }

    protected abstract List<T> getAdapterData();

    protected abstract List<T> getDisconnectedAdapterData();

    // Abstract methods
    protected abstract void onSendMessage(final String message);

    public abstract FragmentType getType();

    // Getters and setters
    public String getTitle() {
        return mTitle;
    }
}