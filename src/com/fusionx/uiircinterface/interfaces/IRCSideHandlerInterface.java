package com.fusionx.uiircinterface.interfaces;

import com.fusionx.irc.handlerabstract.ChannelHandler;
import com.fusionx.irc.handlerabstract.ServerHandler;
import com.fusionx.irc.handlerabstract.UserHandler;

public interface IRCSideHandlerInterface {
    public ServerHandler getServerHandler();

    public ChannelHandler getChannelHandler(final String channelName);

    public UserHandler getUserHandler(final String userNick);
}