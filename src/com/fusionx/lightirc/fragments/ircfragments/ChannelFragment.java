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
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fusionx.Utils;
import com.fusionx.irc.Channel;
import com.fusionx.irc.ChannelUser;
import com.fusionx.irc.Server;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ChannelEventType;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.handlerabstract.ChannelFragmentHandler;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;
import com.fusionx.uiircinterface.MessageParser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import lombok.Getter;

public class ChannelFragment extends IRCFragment {
    private ChannelFragmentCallback mCallback;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String message = String.format(getActivity().getString(R.string
                .parser_joined_channel), mCallback.getServer
                (false).getUser().getColorfulNick());
        appendToTextView(message + "\n");
    }

    @Override
    public void onResume() {
        super.onResume();

        final Server server = mCallback.getServer(true);
        if (server != null) {
            final Channel channel = server.getUserChannelInterface().getChannel(getTitle());
            if (channel != null) {
                writeToTextView(channel.getBuffer());
            }
        }
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

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final CharSequence text = mEditText.getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && StringUtils.isNotEmpty(text)) {
            final String message = text.toString();
            mEditText.setText("");

            sendChannelMessage(getTitle(), message);
        }
        return false;
    }

    public void onUserMention(final ArrayList<ChannelUser> users) {
        final String text = String.valueOf(mEditText.getText());
        String nicks = "";
        for (final ChannelUser userNick : users) {
            nicks += Html.fromHtml(userNick.getPrettyNick(getTitle())) + ": ";
        }

        mEditText.clearComposingText();
        mEditText.setText(nicks + text);
        mEditText.requestFocus();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.Channel;
    }

    public void sendChannelMessage(final String channelName, final String message) {
        MessageParser.channelMessageToParse(mCallback, channelName, message);
    }

    public interface ChannelFragmentCallback extends CommonCallbacks {
        public void switchFragmentAndRemove(final String channelName);

        public void updateUserList(final String channelName);
    }

    @Getter
    private final ChannelFragmentHandler channelFragmentHandler = new ChannelFragmentHandler() {
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