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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fusionx.irc.Server;
import com.fusionx.lightirc.interfaces.CommonCallbacks;
import com.fusionx.lightirc.misc.FragmentType;

public class ServerFragment extends IRCFragment {
    private ServerFragmentCallback mCallback;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO - fix this up
        //getEditText().setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Server server = mCallback.getServer(true);
        if (server != null) {
            writeToTextView(server.getBuffer());
        }
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
    }
}