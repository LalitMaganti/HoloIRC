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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fusionx.irc.Channel;
import com.fusionx.irc.User;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.uiircinterface.MessageSender;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.lightirc.misc.Utils;
import com.fusionx.lightirc.parser.MessageParser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ChannelFragment extends IRCFragment {
    private ChannelFragmentCallback mCallback;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String message = String.format(getActivity().getString(R.string
                .parser_joined_channel),
                mCallback.getServer(false).getUser().getColorfulNick());
        appendToTextView(message + "\n");
    }

    @Override
    public void onResume() {
        super.onResume();

        MessageSender.getSender(mCallback.getServer(false).getTitle()).registerChannelFragmentHandler
                (getTitle(), mChannelFragmentHandler);

        final Channel channel = mCallback.getServer(false).getUserChannelInterface().getChannel
                (getTitle());
        if (channel != null) {
            writeToTextView(channel.getBuffer());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        MessageSender.getSender(mCallback.getServer(false).getTitle())
                .unregisterChannelFragmentHandler(getTitle());
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

    private final Handler mChannelFragmentHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ChannelEventType type = (ChannelEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            switch (type) {
                case UserListChanged:
                    mCallback.updateUserList(getTitle());
                    if (!Utils.isMessagesFromChannelShown(getActivity())) {
                        break;
                    }
                case Generic:
                    appendToTextView(bundle.getString(EventBundleKeys.message) + "\n");
                    break;
                case UserParted:
                    mCallback.switchFragmentAndRemove(getTitle());
                    break;
            }
        }
    };
}