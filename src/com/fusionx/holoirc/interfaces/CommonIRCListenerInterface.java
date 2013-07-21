package com.fusionx.holoirc.interfaces;

public interface CommonIRCListenerInterface {
    public void onCreatePMFragment(final String userNick);

    public void closeAllSlidingMenus();

    public boolean isConnectedToServer();

    public void selectServerFragment();
}
