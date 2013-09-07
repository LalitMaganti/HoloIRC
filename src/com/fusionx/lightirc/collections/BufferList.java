package com.fusionx.lightirc.collections;

import android.text.Spanned;

import java.util.ArrayList;

public class BufferList extends ArrayList<Spanned> {
    private final Object mLock = new Object();

    public Object getLock() {
        return mLock;
    }
}