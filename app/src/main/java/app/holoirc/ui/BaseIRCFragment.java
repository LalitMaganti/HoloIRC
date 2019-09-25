package app.holoirc.ui;

import app.holoirc.misc.FragmentType;

import androidx.fragment.app.Fragment;

import co.fusionx.relay.base.Conversation;

public abstract class BaseIRCFragment extends Fragment {

    public abstract FragmentType getType();

    public abstract boolean isValid();

    public abstract Conversation getConversation();
}