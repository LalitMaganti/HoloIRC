package com.fusionx.lightirc.interfaces;

import com.fusionx.irc.Server;

public interface CommonCallbacks {
    public void onCreatePMFragment(final String userNick);

    public void closeAllSlidingMenus();

    public boolean isConnectedToServer();

    public void selectServerFragment();

    public Server getServer(final boolean nullAllowed);
}
