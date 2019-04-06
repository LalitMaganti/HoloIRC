package com.fusionx.lightirc.ui;

import com.fusionx.lightirc.misc.FragmentType;

import android.support.v4.app.Fragment;

import co.fusionx.relay.base.Conversation;
import co.fusionx.relay.event.Event;

public abstract class BaseIRCFragment extends Fragment {

    public abstract FragmentType getType();

    public abstract boolean isValid();

    public abstract Conversation getConversation();
}