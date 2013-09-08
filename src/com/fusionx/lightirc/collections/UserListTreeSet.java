package com.fusionx.lightirc.collections;

import com.fusionx.lightirc.interfaces.SynchronizedCollection;
import com.fusionx.lightirc.irc.ChannelUser;

import java.util.Comparator;

public class UserListTreeSet extends UpdateableTreeSet<ChannelUser> implements
        SynchronizedCollection<ChannelUser> {
     public UserListTreeSet(Comparator<ChannelUser> comparator) {
         super(comparator);
     }

    private Object mLock = new Object();
    @Override
    public Object getLock() {
        return mLock;
    }
}