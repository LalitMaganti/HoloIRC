package com.fusionx.lightirc.interfaces;

import java.util.Collection;

public interface SynchronizedCollection<T> extends Collection<T> {

    public Object getLock();
}