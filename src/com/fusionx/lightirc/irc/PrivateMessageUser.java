package com.fusionx.lightirc.irc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.fusionx.lightirc.constants.EventBundleKeys;
import com.fusionx.lightirc.constants.UserEventTypeEnum;
import com.fusionx.lightirc.irc.writers.UserWriter;
import com.fusionx.lightirc.util.MiscUtils;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public class PrivateMessageUser extends User {
    protected String buffer = "";

    @Getter
    protected final UserWriter writer;

    public PrivateMessageUser(@NonNull String nick, @NonNull UserChannelInterface userChannelInterface) {
        super(nick, userChannelInterface);

        writer = new UserWriter(userChannelInterface.getOutputStream(), this);
    }

    private Handler userHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final Bundle event = msg.getData();
            final UserEventTypeEnum type = (UserEventTypeEnum) event
                    .getSerializable(EventBundleKeys.eventType);
            final String message = event.getString(EventBundleKeys.message);
            switch (type) {
                case Generic:
                    buffer += message + "\n";
                    break;
            }
        }
    };

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
        return MiscUtils.areNicksEqual(nick, otherNick);
    }
}