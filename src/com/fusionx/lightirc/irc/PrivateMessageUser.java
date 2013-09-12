package com.fusionx.lightirc.irc;

import android.os.Handler;
import android.text.Spanned;

import com.fusionx.lightirc.adapters.IRCMessageAdapter;
import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.irc.writers.UserWriter;
import com.fusionx.lightirc.util.ColourParserUtils;
import com.fusionx.lightirc.util.IRCUtils;

import java.util.ArrayList;

import lombok.Data;

@Data
public class PrivateMessageUser extends User {
    protected final UserWriter writer;
    private final Handler mAdapterHandler;
    protected IRCMessageAdapter buffer;

    public PrivateMessageUser(final String nick, final UserChannelInterface userChannelInterface,
                              final Handler adapterHandler) {
        super(nick, userChannelInterface);
        writer = new UserWriter(userChannelInterface.getOutputStream(), this);
        mAdapterHandler = adapterHandler;
        mAdapterHandler.post(new Runnable() {
            @Override
            public void run() {
                buffer = new IRCMessageAdapter(userChannelInterface.getContext(),
                        new ArrayList<Spanned>());
            }
        });
    }

    public void onUserEvent(final UserEvent event) {
        if (nick.equals(event.userNick)) {
            mAdapterHandler.post(new Runnable() {
                @Override
                public void run() {
                    buffer.add(ColourParserUtils.parseHtml(event.message));
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