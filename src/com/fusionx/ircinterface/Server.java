package com.fusionx.ircinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.misc.BroadcastSender;
import com.fusionx.ircinterface.writers.ServerWriter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Server {
    protected ServerWriter writer;
    protected UserChannelInterface userChannelInterface;

    protected final LocalBroadcastManager broadcastManager;
    protected final BroadcastSender broadcastSender;

    protected final String title;
    protected AppUser user;

    @Setter(AccessLevel.NONE)
    protected String buffer = "";
    protected String status = "Disconnected";
    protected String MOTD = "";

    public Server(final String serverTitle,
                  final BroadcastSender sender) {
        title = serverTitle;
        broadcastSender = sender;
        broadcastManager = sender.getBroadcastManager();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(EventDestination.Server);
        final ServerBufferListener listener = new ServerBufferListener();
        broadcastManager.registerReceiver(listener, filter);
    }

    private class ServerBufferListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Event event = intent.getParcelableExtra("event");
            final ServerEventType type = (ServerEventType) event.getType();
            switch (type) {
                case ServerConnected:
                case Generic:
                case Error:
                    buffer += event.getMessage()[0] + "\n";
                    break;
            }
        }
    }
}