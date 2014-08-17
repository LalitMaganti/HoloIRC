package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.misc.FragmentType;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import co.fusionx.relay.dcc.event.file.DCCFileEvent;

public class DCCFileFragment extends IRCFragment<DCCFileEvent> {

    @Override
    public View onCreateView(final LayoutInflater inflate, final ViewGroup container,
            final Bundle savedInstanceState) {
        return super.onCreateView(inflate, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public FragmentType getType() {
        return FragmentType.DCCFILE;
    }

    @Override
    protected List<DCCFileEvent> getAdapterData() {
        return null;
    }

    @Override
    protected void onSendMessage(final String message) {

    }

    @Override
    public boolean isValid() {
        return false;
    }
}