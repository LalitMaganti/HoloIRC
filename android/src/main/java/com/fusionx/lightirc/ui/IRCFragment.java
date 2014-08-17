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
import com.fusionx.lightirc.misc.EventCache;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.lightirc.util.UIUtils;

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

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.event.Event;

import static com.fusionx.lightirc.util.MiscUtils.getBus;

abstract class IRCFragment<T extends Event> extends ListFragment implements TextView
        .OnEditorActionListener {

    Conversation mConversation;

    EditText mMessageBox;

    String mTitle;

    IRCMessageAdapter<T> mMessageAdapter;

    private Object mEventListener = new Object() {
        @Subscribe
        public void onEvent(final OnPreferencesChangedEvent event) {
            onResetBuffer(null);

            // Fix for http://stackoverflow.com/questions/12049198/how-to-clear-the-views-which-are
            // -held-in-the-listviews-recyclebin/16261588#16261588
            getListView().setAdapter(mMessageAdapter);
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
            final Bundle savedInstanceState) {
        return createView(container, inflate);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final OnConversationChanged event = getBus().getStickyEvent(OnConversationChanged.class);
        mConversation = event.conversation;

        mMessageBox = UIUtils.findById(view, R.id.fragment_irc_message_box);
        mMessageBox.setOnEditorActionListener(this);

        mTitle = getArguments().getString("title");

        getBus().register(mEventListener);
        mMessageAdapter = getNewAdapter();
        setListAdapter(mMessageAdapter);

        onResetBuffer(() -> {
            // While the processing is occurring we could have destroyed the view by rotation
            if (getView() != null) {
                if (savedInstanceState == null) {
                    getListView().setSelection(mMessageAdapter.getCount() - 1);
                } else {
                    getListView().onRestoreInstanceState(savedInstanceState.getParcelable
                            ("list_view"));
                }
            }
        });
        mConversation.getServer().getServerEventBus().register(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getView() != null) {
            outState.putParcelable("list_view", getListView().onSaveInstanceState());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getBus().unregister(mEventListener);
        mConversation.getServer().getServerEventBus().unregister(this);
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

    public List<T> onResetBuffer(final Runnable runnable) {
        final List<T> list = getAdapterData();
        mMessageAdapter.setData(list, runnable);
        return list;
    }

    public abstract FragmentType getType();

    // Getters and setters
    public String getTitle() {
        return mTitle;
    }

    protected IRCMessageAdapter<T> getNewAdapter() {
        final Callback callback = FragmentUtils.getParent(this, Callback.class);
        return new IRCMessageAdapter<>(getActivity(), callback.getEventCache(), true);
    }

    protected View createView(final ViewGroup container, final LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_irc, container, false);
    }

    protected abstract List<T> getAdapterData();

    // Abstract methods
    protected abstract void onSendMessage(final String message);

    public abstract boolean isValid();

    public interface Callback {

        public EventCache getEventCache();
    }
}
