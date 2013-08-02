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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.fusionx.ircinterface.Server;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;

public class ServerFragment extends IRCFragment {
    private ServerFragmentCallback mCallback;
    private ServerListener mListener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getEditText().setEnabled(false);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        final Server server = mCallback.getServer(true);
        if (server != null) {
            writeToTextView(server.getBuffer());
        }

        final IntentFilter filter = new IntentFilter();
        filter.addAction(EventDestination.Server);
        mListener = new ServerListener();
        mCallback.getBroadcastManager().registerReceiver(mListener, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        mCallback.getBroadcastManager().unregisterReceiver(mListener);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (ServerFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement ServerFragmentCallback");
        }
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final CharSequence text = getEditText().getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && text != null && !text.equals("")) {
            final String message = text.toString();
            getEditText().setText("");

            mCallback.sendServerMessage(message);
        }
        return false;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.Server;
    }

    public interface ServerFragmentCallback extends CommonCallbacks {
        public void sendServerMessage(final String message);

        public void onNewChannelJoined(final String channelName, final boolean forceSwitch);
    }

    private class ServerListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final Event event = intent.getParcelableExtra("event");
            final ServerEventType type = (ServerEventType) event.getType();
            switch (type) {
                case Join:
                    mCallback.onNewChannelJoined(event.getMessage()[0], true);
                    break;
                case Error:
                    appendToTextView(event.getMessage()[0] + "\n");
                    break;
                case NewPrivateMessage:
                    mCallback.onCreatePMFragment(event.getMessage()[0]);
                    break;
                case ServerConnected:
                    getEditText().setEnabled(true);
                    // FALL THROUGH INTENTIONAL
                case Generic:
                    appendToTextView(event.getMessage()[0] + "\n");
                    break;
            }
        }
    }
}