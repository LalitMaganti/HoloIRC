package com.fusionx.irc;

import android.os.Bundle;
import android.os.Message;

import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerChannelEventType;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.handlerabstract.ServerHandler;
import com.fusionx.irc.misc.Utils;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.uiircinterface.MessageSender;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Server {
    protected ServerWriter writer;
    protected UserChannelInterface userChannelInterface;

    protected final String title;
    protected AppUser user;

    @Setter(AccessLevel.NONE)
    protected String buffer = "";
    protected String status = "Disconnected";
    protected String MOTD = "";

    public Server(final String serverTitle) {
        title = serverTitle;

        MessageSender.getSender(serverTitle).registerServerHandler(serverHandler);
    }

    public void privateMessageSent(final User sendingUser, final String message) {
        final MessageSender sender = MessageSender.getSender(title);
        if (!user.isPrivateMessageOpen(sendingUser)) {
            user.newPrivateMessage(sendingUser);

            if(StringUtils.isNotEmpty(message)) {
                sender.sendPrivateMessage(sendingUser, message);
            }

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerChannelEventType.NewPrivateMessage, sendingUser.getNick());
            sender.sendServerChannelMessage(event);
        } else {
            sender.sendPrivateMessage(sendingUser, message);
        }
    }

    public void privateActionSent(final User sendingUser, final String action) {
        final MessageSender sender = MessageSender.getSender(title);
        if (!user.isPrivateMessageOpen(sendingUser)) {
            user.newPrivateMessage(sendingUser);

            sender.sendAction(sendingUser.getNick(), sendingUser, action);

            final Bundle event = Utils.parcelDataForBroadcast(null,
                    ServerChannelEventType.NewPrivateMessage, sendingUser.getNick());
            sender.sendServerMessage(event);
        } else {
            sender.sendAction(sendingUser.getNick(), sendingUser, action);
        }
    }

    private ServerHandler serverHandler = new ServerHandler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            final ServerEventType type = (ServerEventType) bundle.getSerializable(EventBundleKeys
                    .eventType);
            switch (type) {
                case ServerConnected:
                case Generic:
                case Error:
                    buffer += bundle.getString(EventBundleKeys.message) + "\n";
                    break;
            }
        }
    };
}