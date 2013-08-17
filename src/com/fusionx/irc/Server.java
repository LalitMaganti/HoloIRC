package com.fusionx.irc;

import android.os.Bundle;
import android.os.Message;

import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.ServerEventType;
import com.fusionx.irc.handlerabstract.ServerHandler;
import com.fusionx.irc.writers.ServerWriter;
import com.fusionx.uiircinterface.MessageSender;

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