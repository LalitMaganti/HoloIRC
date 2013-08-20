package com.fusionx.uiircinterface.interfaces;

import com.fusionx.lightirc.handlerabstract.ChannelFragmentHandler;
import com.fusionx.lightirc.handlerabstract.PMFragmentHandler;
import com.fusionx.lightirc.handlerabstract.ServerChannelHandler;
import com.fusionx.lightirc.handlerabstract.ServerFragHandler;

public interface FragmentSideHandlerInterface {
    public ServerChannelHandler getServerChannelHandler();

    public ServerFragHandler getServerFragmentHandler();

    public ChannelFragmentHandler getChannelFragmentHandler(String channelName);

    public PMFragmentHandler getUserFragmentHandler(String userNick);

    public void mention(final String destination);
}
