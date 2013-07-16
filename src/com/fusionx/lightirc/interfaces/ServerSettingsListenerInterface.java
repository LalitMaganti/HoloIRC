package com.fusionx.lightirc.interfaces;

public interface ServerSettingsListenerInterface {
    public String getFileName();
    public boolean getNewServer();
    public void setCanExit(boolean canExit);
}