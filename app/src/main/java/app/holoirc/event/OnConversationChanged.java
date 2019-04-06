package app.holoirc.event;

import app.holoirc.misc.FragmentType;

import co.fusionx.relay.base.Conversation;

public class OnConversationChanged {

    public final Conversation conversation;

    public final FragmentType fragmentType;

    public OnConversationChanged(final Conversation conversation, final FragmentType fragmentType) {
        this.conversation = conversation;
        this.fragmentType = fragmentType;
    }
}