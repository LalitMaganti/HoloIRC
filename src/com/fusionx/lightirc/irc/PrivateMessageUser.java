package com.fusionx.lightirc.irc;

import android.text.Html;
import android.text.Spanned;

import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.irc.writers.UserWriter;
import com.fusionx.lightirc.util.IRCUtils;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

public class PrivateMessageUser extends User {
    @Getter
    protected final ArrayList<Spanned> buffer = new ArrayList<>();

    @Getter
    protected final UserWriter writer;

    public PrivateMessageUser(@NonNull String nick, @NonNull UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);

        writer = new UserWriter(userChannelInterface.getOutputStream(), this);
    }

    public void onUserEvent(UserEvent event) {
        if(nick.equals(event.userNick)) {
            buffer.add(Html.fromHtml(event.message));
        }
    }

    @Override
    public boolean equals(final Object o) {
        String otherNick;
        if (o instanceof PrivateMessageUser) {
            otherNick = ((PrivateMessageUser) o).getNick();
        } else if (o instanceof String) {
            otherNick = (String) o;
        } else {
            return false;
        }
        return IRCUtils.areNicksEqual(nick.toString(), otherNick);
    }
}