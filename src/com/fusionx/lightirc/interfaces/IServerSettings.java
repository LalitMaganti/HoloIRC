package com.fusionx.lightirc.interfaces;

public interface IServerSettings {
    public String getFileName();

    public boolean canSaveChanges();

    public void setCanSaveChanges(final boolean canSave);
}