package com.fusionx.lightirc.serversettings;

public interface IServerSettings {
    public String getFileName();

    public boolean canSaveChanges();

    public void setCanSaveChanges(final boolean canSave);
}