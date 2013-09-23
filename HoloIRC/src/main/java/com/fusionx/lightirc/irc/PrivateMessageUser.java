package com.fusionx.lightirc.irc;

import android.os.Handler;

import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.irc.writers.UserWriter;
import com.fusionx.lightirc.util.IRCUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public final class PrivateMessageUser extends User {
    protected final UserWriter writer;
    private final Handler mAdapterHandler;
    private IRCMessageAdapter buffer;

    public PrivateMessageUser(final String nick, final UserChannelInterface userChannelInterface,
                              final Handler adapterHandler) {
        super(nick, userChannelInterface);
        writer = new UserWriter(userChannelInterface.getOutputStream(), this);
        mAdapterHandler = adapterHandler;
        mAdapterHandler.post(new Runnable() {
            @Override
            public void run() {
                buffer = new IRCMessageAdapter(userChannelInterface.getContext(),
                        new ArrayList<Message>());
            }
        });
    }

    public void onUserEvent(final UserEvent event) {
        if (StringUtils.isNotBlank(event.message)) {
            mAdapterHandler.post(new Runnable() {
                @Override
                public void run() {
                    buffer.add(new Message(event.message));
                }
            });
        }
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
}
