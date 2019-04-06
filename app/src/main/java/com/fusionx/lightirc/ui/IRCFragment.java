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

import com.fusionx.bus.Subscribe;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.event.OnConversationChanged;
import com.fusionx.lightirc.event.OnPreferencesChangedEvent;
import com.fusionx.lightirc.event.OnServerStatusChanged;
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.util.FragmentUtils;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import co.fusionx.relay.base.ConnectionStatus;
import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.event.Event;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

abstract class IRCFragment<T extends Event> extends BaseIRCFragment
        implements TextView.OnEditorActionListener {

    Conversation mConversation;

    EditText mMessageBox;

    String mTitle;

    IRCAdapter<T> mMessageAdapter;

    RecyclerView mRecyclerView;

    LinearLayoutManager mLayoutManager;

    private Object mEventListener = new Object() {
        @Subscribe
        public void onEvent(final OnPreferencesChangedEvent event) {
            onResetBuffer(null);
        }

        @Subscribe
        public void onEvent(final OnServerStatusChanged event) {
            updateConnectedState();
        }
    };

    private class BottomPinningLayoutEventListener
            extends RecyclerView.OnScrollListener
            implements View.OnLayoutChangeListener {
        private boolean mIsScrolledToBottom = true;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            mIsScrolledToBottom = mLayoutManager.findLastCompletelyVisibleItemPosition()
                    == mMessageAdapter.getItemCount() - 1;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                   int oldTop, int oldRight, int oldBottom) {
            if ((bottom < oldBottom) && mIsScrolledToBottom) {
                mRecyclerView.post(
                        () -> mRecyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1));
            }
        }
    }

    private BottomPinningLayoutEventListener mBottomPinningLayoutEventListener =
            new BottomPinningLayoutEventListener();

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflate.inflate(R.layout.fragment_irc, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addOnLayoutChangeListener(mBottomPinningLayoutEventListener);
        mRecyclerView.addOnScrollListener(mBottomPinningLayoutEventListener);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ViewCompat.setOverScrollMode(mRecyclerView, ViewCompat.OVER_SCROLL_NEVER);

        final OnConversationChanged event = getBus().getStickyEvent(OnConversationChanged.class);
        mConversation = event.conversation;

        mMessageBox = (EditText) view.findViewById(R.id.fragment_irc_message_box);
        mMessageBox.setOnEditorActionListener(this);

        mTitle = getArguments().getString("title");

        getBus().register(mEventListener);
        mMessageAdapter = getNewAdapter();
        mRecyclerView.setAdapter(mMessageAdapter);

        onResetBuffer(() -> {
        });
        mConversation.getBus().register(this);
        updateConnectedState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventListener);
        mConversation.getBus().unregister(this);
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

    public List<? extends T> onResetBuffer(final Runnable runnable) {
        final List<? extends T> list = getAdapterData();
        mMessageAdapter.setData(list, () -> {
            if (runnable != null) {
                runnable.run();
            }
            mRecyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        });
        return list;
    }

    @Override
    public Conversation getConversation() {
        return mConversation;
    }

    @Override
    public boolean isValid() {
        return mConversation.isValid();
    }

    private void updateConnectedState() {
        if (mMessageBox != null) {
            ConnectionStatus status = mConversation.getServer().getStatus();
            mMessageBox.setEnabled(
                    status == ConnectionStatus.REGISTERING || status == ConnectionStatus.CONNECTED);
        }
    }

    // Getters and setters
    public String getTitle() {
        return mTitle;
    }

    protected IRCAdapter<T> getNewAdapter() {
        final Callback callback = FragmentUtils.getParent(this, Callback.class);
        return new IRCAdapter<>(getActivity(), callback.getEventCache(mConversation), true);
    }

    // Abstract methods
    protected abstract List<? extends T> getAdapterData();

    protected abstract void onSendMessage(final String message);

    public interface Callback {

        public EventCache getEventCache(final Conversation conversation);
    }
}
