package com.fusionx.lightirc.serversettings;

public interface IServerSettings {
    public String getFileName();

    //public void openAutoJoinList();

    //public void openBaseFragment();

    public boolean canSaveChanges();

    public void setCanSaveChanges(final boolean canSave);
}