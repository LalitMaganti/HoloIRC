package com.fusionx.lightirc.interfaces;

public interface ServerSettingsCallbacks {
    public String getFileName();

    public void openAutoJoinList();

    public void openBaseFragment();

    public boolean canSaveChanges();

    public void setCanSaveChanges(final boolean canSave);
}