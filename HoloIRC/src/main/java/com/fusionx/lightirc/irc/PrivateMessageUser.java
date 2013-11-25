package com.fusionx.lightirc.irc;

import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.irc.writers.UserWriter;
import com.fusionx.lightirc.util.IRCUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PrivateMessageUser extends User {
    /**
     * This is the object that allows sending of messages directly to the server
     */
    private final UserWriter mWriter;

    /**
     * Contains a copy of the messages when the conversation is not displayed to the user
     */
    private final List<Message> mBuffer = new ArrayList<Message>();

    /**
     * Stores whether the fragment corresponding to this conversation is cached by the
     * FragmentManager
     */
    private boolean mCached;

    /**
     * Constructor
     *
     * @param nick - the nickname of the user we're having the conversation with
     * @param userChannelInterface - a copy of the user channel interface
     */
    public PrivateMessageUser(final String nick, final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
        mWriter = new UserWriter(userChannelInterface.getOutputStream(), this);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof PrivateMessageUser) {
            String otherNick = ((PrivateMessageUser) o).getNick();
            return IRCUtils.areNicksEqual(mNick, otherNick);
        } else {
            return false;
        }
    }

    public void onUserEvent(final UserEvent event) {
        if (StringUtils.isNotBlank(event.message)) {
            synchronized (mBuffer) {
                mBuffer.add(new Message(event.message));
            }
        }
    }

    // Getters and Setters
    public UserWriter getWriter() {
        return mWriter;
    }

    public List<Message> getBuffer() {
        return mBuffer;
    }

    public boolean isCached() {
        return mCached;
    }

    public void setCached(boolean cached) {
        mCached = cached;
    }
}
