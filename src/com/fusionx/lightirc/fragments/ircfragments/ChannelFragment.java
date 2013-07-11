/*
    LightIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of LightIRC.

    LightIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LightIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LightIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.fragments.ircfragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.fusionx.lightirc.parser.ServerCommunicator;
import org.pircbotx.Channel;

import java.util.Set;

public class ChannelFragment extends IRCFragment {
    private ChannelFragmentListenerInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ChannelFragmentListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChannelFragmentListenerInterface");
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        final CharSequence text = getEditText().getText();
        if (i == EditorInfo.IME_ACTION_DONE && text != null && !text.equals("")) {
            final String message = text.toString();
            getEditText().setText("");

            mListener.sendChannelMessage(getTitle(), message);
        }
        return false;
    }

    public void onUserMention(final Set<String> users) {
        for (final String userNick : users) {
            String edit = getEditText().getText().toString();
            edit = Html.fromHtml(userNick) + ": " + edit;
            getEditText().clearComposingText();
            getEditText().append(edit);
            getEditText().requestFocus();
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        writeToTextView(mListener.getChannel(getTitle()).getBuffer());
    }

    @Override
    public void partOrCloseIRC(final boolean channel) {
        if (channel) {
            ServerCommunicator.sendPart(mListener.getChannel(getTitle()), getActivity().getApplicationContext());
        }
    }

    public interface ChannelFragmentListenerInterface {
        public Channel getChannel(final String channelName);

        public void sendChannelMessage(final String channelName, final String message);
    }
}