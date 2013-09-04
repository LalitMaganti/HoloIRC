package com.fusionx.lightirc.uiircinterface.interfaces;

import android.os.Handler;

public interface IIRCSideHandler {
    public Handler getServerHandler();

    public Handler getChannelHandler(final String channelName);

    public Handler getUserHandler(final String userNick);

    public String getTitle();

    public String getNick();
}