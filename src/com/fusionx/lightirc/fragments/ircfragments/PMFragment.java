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
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.fusionx.lightirc.parser.ServerCommunicator;
import org.pircbotx.User;

public class PMFragment extends IRCFragment {
    private PMFragmentListenerInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PMFragmentListenerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PMFragmentListenerInterface");
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            final String message = getEditText().getText().toString();
            getEditText().setText("");

            mListener.sendUserMessage(getTitle(), message);
        }
        return false;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        final String buffer = mListener.getUser(getTitle()).getBuffer();
        writeToTextView(buffer);
    }

    @Override
    public void partOrCloseIRC(final boolean channel) {
        if (!channel) {
            ServerCommunicator.sendClosePrivateMessage(mListener.getUser(getTitle()));
        }
    }

    public interface PMFragmentListenerInterface {
        public User getUser(final String channelName);

        public void sendUserMessage(final String nick, final String message);
    }
}
