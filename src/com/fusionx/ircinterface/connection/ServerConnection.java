package com.fusionx.ircinterface.connection;

import android.content.Context;
import android.content.IntentFilter;
import com.fusionx.ircinterface.AppUser;
import com.fusionx.ircinterface.Server;
import com.fusionx.ircinterface.ServerConfiguration;
import com.fusionx.ircinterface.UserChannelInterface;
import com.fusionx.ircinterface.constants.EventDestination;
import com.fusionx.ircinterface.enums.ServerEventType;
import com.fusionx.ircinterface.events.Event;
import com.fusionx.ircinterface.listeners.CoreListener;
import com.fusionx.ircinterface.misc.BroadcastSender;
import com.fusionx.ircinterface.misc.Utils;
import com.fusionx.ircinterface.parser.ServerConnectionParser;
import com.fusionx.ircinterface.parser.ServerLineParser;
import com.fusionx.ircinterface.writers.ServerWriter;
import com.fusionx.lightirc.R;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

class ServerConnection {
    @Getter(AccessLevel.PACKAGE)
    private Server server;
    private final BroadcastSender broadcastSender;
    private final Context mContext;

    private final ServerConfiguration serverConfiguration;
    private Socket mSocket;

    private CoreListener coreListener;

    ServerConnection(final ServerConfiguration configuration, final Context context,
                     final BroadcastSender sender) {
        broadcastSender = sender;
        server = new Server(configuration.getTitle(), broadcastSender);

        serverConfiguration = configuration;
        mContext = context;
    }

    void connectToServer() throws InterruptedException {
        try {
            final SSLSocketFactory sslSocketFactory = (SSLSocketFactory)
                    SSLSocketFactory.getDefault();

            mSocket = serverConfiguration.isSsl() ?
                    sslSocketFactory.createSocket(serverConfiguration.getUrl(),
                            serverConfiguration.getPort()) :
                    new Socket(serverConfiguration.getUrl(), serverConfiguration.getPort());

            final OutputStreamWriter writer = new OutputStreamWriter(mSocket.getOutputStream());
            server.setWriter(new ServerWriter(writer));

            server.setStatus(mContext.getString(R.string.status_connecting));

            final UserChannelInterface userChannelInterface = new UserChannelInterface(writer,
                    broadcastSender.getBroadcastManager(), mContext);
            server.setUserChannelInterface(userChannelInterface);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(EventDestination.Core);

            coreListener = new CoreListener(server.getWriter());
            broadcastSender.getBroadcastManager().registerReceiver(coreListener, filter);

            if (StringUtils.isNotEmpty(serverConfiguration.getServerPassword())) {
                server.getWriter().sendServerPassword(serverConfiguration.getServerPassword());
            }

            server.getWriter().changeNick(serverConfiguration.getNick());
            server.getWriter().sendUser(serverConfiguration.getServerUserName(), "8", "*",
                    StringUtils.isNotEmpty(serverConfiguration.getRealName()) ?
                            serverConfiguration.getRealName() : "HoloIRC");

            final String channel = "#holoirc";

            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream()));
            final String nick = ServerConnectionParser.parseConnect(broadcastSender, reader);

            final Event event = Utils.parcelDataForBroadcast(EventDestination.Server, null,
                    ServerEventType.ServerConnected, String.format(mContext
                    .getString(R.string.parser_connected),
                    serverConfiguration.getUrl()));
            broadcastSender.sendBroadcast(event);

            server.setStatus(mContext.getString(R.string.status_connected));

            if (nick != null) {
                final AppUser user = new AppUser(nick, server.getUserChannelInterface());
                server.setUser(user);

                server.getWriter().joinChannel(channel);

                final ServerLineParser parser = new ServerLineParser(broadcastSender, server, mContext);
                parser.parseMain(reader);
            } else {
                // An error has occurred - TODO - find out which
            }
        } catch (final IOException ex) {
            // Delay is to allow event to be sent while activity is visible
            Thread.sleep(1000);

            final Event event = Utils.parcelDataForBroadcast(EventDestination.Server, null,
                    ServerEventType.Error, ex.getMessage());
            broadcastSender.sendBroadcast(event);

            server.setStatus(mContext.getString(R.string.status_disconnected));
        }
    }

    void disconnectFromServer() {
        server.getWriter().quitServer("");
        broadcastSender.getBroadcastManager().unregisterReceiver(coreListener);
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}