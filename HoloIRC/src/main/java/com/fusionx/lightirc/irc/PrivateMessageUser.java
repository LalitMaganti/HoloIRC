package com.fusionx.lightirc.irc;

import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.irc.writers.UserWriter;
import com.fusionx.lightirc.util.IRCUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public final class PrivateMessageUser extends User {
    protected final UserWriter mWriter;
    private List<Message> mBuffer;
    private boolean mCached;

    public PrivateMessageUser(final String nick, final UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);
        mWriter = new UserWriter(userChannelInterface.getOutputStream(), this);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof PrivateMessageUser) {
            String otherNick = ((PrivateMessageUser) o).getNick();
            return IRCUtils.areNicksEqual(nick, otherNick);
        } else {
            return false;
        }
    }

    public void onUserEvent(final UserEvent event) {
        if (StringUtils.isNotBlank(event.message) && mBuffer != null) {
            mBuffer.add(new Message(event.message));
        }
    }

    // Getters and Setters
    public UserWriter getWriter() {
        return mWriter;
    }

    public List<Message> getBuffer() {
        return mBuffer;
    }
    public void setBuffer(final List<Message> buffer) {
        mBuffer = buffer;
    }

    public boolean isCached() {
        return mCached;
    }
    public void setCached(boolean cached) {
        mCached = cached;
    }
}
