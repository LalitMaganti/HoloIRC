package com.fusionx.lightirc.interfaces;

public interface CommonIRCListenerInterface {
    public void onCreatePMFragment(final String userNick);

    public void closeAllSlidingMenus();

    public boolean isConnectedToServer();
}
