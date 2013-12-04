package com.fusionx.lightirc.collections;

import com.fusionx.androidirclibrary.interfaces.SynchronizedCollection;

import java.util.ArrayList;

public class SynchronizedArrayList<T> extends ArrayList<T> implements SynchronizedCollection<T> {

    private final Object mLock = new Object();

    @Override
    public Object getLock() {
        return mLock;
    }

    public SynchronizedArrayList() {
        super();
    }
}