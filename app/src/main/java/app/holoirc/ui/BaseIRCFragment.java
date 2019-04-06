package app.holoirc.ui;

import app.holoirc.misc.FragmentType;

import android.support.v4.app.Fragment;

import co.fusionx.relay.base.Conversation;

public abstract class BaseIRCFragment extends Fragment {

    public abstract FragmentType getType();

    public abstract boolean isValid();

    public abstract Conversation getConversation();
}