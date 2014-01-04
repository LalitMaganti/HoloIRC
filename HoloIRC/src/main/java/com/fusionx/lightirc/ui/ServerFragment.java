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

import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.loaders.ServerLoader;
import com.fusionx.lightirc.util.FragmentUtils;
import com.fusionx.relay.Server;
import com.fusionx.relay.ServerStatus;
import com.fusionx.relay.event.server.ServerEvent;
import com.fusionx.relay.parser.UserInputParser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class ServerFragment extends IRCFragment<ServerEvent> {

    private Callbacks mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mCallback == null) {
            mCallback = FragmentUtils.getParent(this, Callbacks.class);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);

        mMessageBox.setEnabled(mCallback.isConnectedToServer());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getServer().getStatus() != ServerStatus.CONNECTED) {
            onDisableUserInput();
        }
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseServerMessage(getServer(), message);
    }

    public void onConnected() {
        mMessageBox.setEnabled(true);
    }

    public Server getServer() {
        return mCallback.getServer();
    }

    @Override
    public FragmentTypeEnum getType() {
        return FragmentTypeEnum.Server;
    }

    @Override
    public Loader<List<ServerEvent>> onCreateLoader(int id, Bundle args) {
        return new ServerLoader(getActivity(), getServer());
    }

    public interface Callbacks {

        public Server getServer();

        public boolean isConnectedToServer();
    }
}