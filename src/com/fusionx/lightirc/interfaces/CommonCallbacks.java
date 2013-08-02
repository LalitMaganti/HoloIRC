package com.fusionx.lightirc.interfaces;

import android.support.v4.content.LocalBroadcastManager;
import com.fusionx.ircinterface.Server;

public interface CommonCallbacks {
    public void onCreatePMFragment(final String userNick);

    public void closeAllSlidingMenus();

    public boolean isConnectedToServer();

    public void selectServerFragment();

    public LocalBroadcastManager getBroadcastManager();

    public Server getServer(final boolean nullAllowed);
}
