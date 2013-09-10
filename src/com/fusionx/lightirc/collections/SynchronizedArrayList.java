package com.fusionx.lightirc.collections;

import com.fusionx.lightirc.interfaces.SynchronizedCollection;

import java.util.ArrayList;
import java.util.Collection;

public class SynchronizedArrayList<T> extends ArrayList<T> implements SynchronizedCollection<T> {
    private final Object object = new Object();

    @Override
    public Object getLock() {
        return object;
    }

    public SynchronizedArrayList() {
        super();
    }

    public SynchronizedArrayList(Collection<T> set) {
        super(set);
    }
}