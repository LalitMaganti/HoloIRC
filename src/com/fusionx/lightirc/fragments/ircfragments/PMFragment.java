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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.fusionx.ircinterface.User;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.UserEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.parser.MessageParser;

public class PMFragment extends IRCFragment {
    private CommonCallbacks mCallback;
    private PMFragmentListener mListener;

    @Override
    public void onStart() {
        super.onStart();

        writeToTextView(getUser(getTitle()).getBuffer());

        final IntentFilter filter = new IntentFilter();
        filter.addAction(EventDestination.User + "." + getTitle());
        mListener = new PMFragmentListener();
        mCallback.getBroadcastManager().registerReceiver(mListener, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        mCallback.getBroadcastManager().unregisterReceiver(mListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (CommonCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement PMFragmentCallbacks");
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        final CharSequence text = getEditText().getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && text != null
                && !text.equals("")) {
            final String message = text.toString();
            getEditText().setText("");

            sendUserMessage(getTitle(), message);
        }
        return false;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.User;
    }

    public User getUser(final String userNick) {
        return mCallback.getServer(false).getUserChannelInterface().getUser(userNick);
    }

    public void sendUserMessage(final String nick, final String message) {
        MessageParser.userMessageToParse(mCallback.getServer(false), nick, message);
    }

    private class PMFragmentListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final Event event = intent.getParcelableExtra("event");
            final UserEventType type = (UserEventType) event.getType();
            switch (type) {
                case Generic:
                    appendToTextView(event.getMessage()[0] + "\n");
            }
        }
    }
}
