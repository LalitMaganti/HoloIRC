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

package com.fusionx.holoirc.fragments.ircfragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        final CharSequence text = getEditText().getText();

        if ((event == null || actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && text != null && !text.equals("")) {
            final String message = text.toString();
            getEditText().setText("");

            mListener.sendChannelMessage(getTitle(), message);
        }
        return false;
    }

    public void onUserMention(final Set<String> users) {
        final String text = String.valueOf(getEditText().getText());
        String nicks = "";
        for (final String userNick : users) {
            nicks += Html.fromHtml(userNick) + ": ";
        }

        getEditText().clearComposingText();
        getEditText().setText(nicks + text);
        getEditText().requestFocus();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        writeToTextView(mListener.getChannel(getTitle()).getBuffer());
    }

    public interface ChannelFragmentListenerInterface {
        public Channel getChannel(final String channelName);

        public void sendChannelMessage(final String channelName, final String message);
    }
}