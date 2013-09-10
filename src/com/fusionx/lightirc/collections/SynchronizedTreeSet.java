package com.fusionx.lightirc.collections;

import com.fusionx.lightirc.interfaces.SynchronizedCollection;

import java.util.Collection;
import java.util.TreeSet;

public class SynchronizedTreeSet<T> extends TreeSet<T> implements SynchronizedCollection<T> {
    private final Object object = new Object();
    @Override
    public Object getLock() {
        return object;
    }

    public SynchronizedTreeSet() {
        super();
    }

    public SynchronizedTreeSet(Collection<T> set) {
        super(set);
    }
}