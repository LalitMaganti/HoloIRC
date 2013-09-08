package com.fusionx.lightirc.interfaces;

import java.util.Collection;

public interface SyncronizedCollection<T> extends Collection<T> {
    public Object getLock();
}