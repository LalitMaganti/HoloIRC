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
import android.text.Html;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.fusionx.ircinterface.User;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ChannelEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.MessageParser;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ChannelFragment extends IRCFragment {
    private ChannelFragmentCallback mCallback;
    private ChannelListener mListener;

    @Override
    public void onStart() {
        super.onStart();

        writeToTextView(mCallback.getServer(false).getUserChannelInterface()
                .getChannel(getTitle()).getBuffer());

        final IntentFilter filter = new IntentFilter();
        filter.addAction(EventDestination.Channel + "." + getTitle());
        mListener = new ChannelListener();
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
            mCallback = (ChannelFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement ChannelFragmentCallback");
        }
    }

    // Options Menu stuff
    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final CharSequence text = getEditText().getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            getEditText().setText("");

            sendChannelMessage(getTitle(), message);
        }
        return false;
    }

    public void onUserMention(final ArrayList<User> users) {
        final String text = String.valueOf(getEditText().getText());
        String nicks = "";
        for (final User userNick : users) {
            nicks += Html.fromHtml(userNick.getPrettyNick(getTitle())) + ": ";
        }

        getEditText().clearComposingText();
        getEditText().setText(nicks + text);
        getEditText().requestFocus();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.Channel;
    }

    public void sendChannelMessage(final String channelName, final String message) {
        MessageParser.channelMessageToParse(getActivity().getApplicationContext(),
                mCallback.getServer(false), channelName, message);
    }

    public interface ChannelFragmentCallback extends CommonCallbacks {
        public void switchFragmentAndRemove(final String channelName);

        public void updateUserList(final String channelName);
    }

    private class ChannelListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final Event event = intent.getParcelableExtra("event");
            final ChannelEventType type = (ChannelEventType) event.getType();
            switch (type) {
                case UserListChanged:
                    mCallback.updateUserList(getTitle());
                    if(!Utils.isMessagesFromChannelShown(getActivity())) {
                        break;
                    }
                case Generic:
                    appendToTextView(event.getMessage()[0] + "\n");
                    break;
                case UserParted:
                    mCallback.switchFragmentAndRemove(getTitle());
                    break;
            }
        }
    }
}