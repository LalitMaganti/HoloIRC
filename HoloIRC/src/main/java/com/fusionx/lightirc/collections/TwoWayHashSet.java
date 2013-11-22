package com.fusionx.lightirc.collections;

import java.util.HashMap;
import java.util.Set;

public abstract class TwoWayHashSet<A extends UpdateableTreeSet.Updateable,
        B extends UpdateableTreeSet.Updateable> {

    protected final HashMap<A, UpdateableTreeSet<B>> aToBMap;

    protected final HashMap<B, UpdateableTreeSet<A>> bToAMap;

    protected TwoWayHashSet() {
        aToBMap = new HashMap<A, UpdateableTreeSet<B>>();
        bToAMap = new HashMap<B, UpdateableTreeSet<A>>();
    }

    protected synchronized void couple(final A objectA, final B objectB) {
        addAToB(objectA, objectB);
        addBToA(objectA, objectB);
    }

    synchronized void addAToB(A objectA, B objectB) {
        UpdateableTreeSet<A> listofA = bToAMap.get(objectB);
        if (listofA == null) {
            listofA = new UpdateableTreeSet<A>();
            bToAMap.put(objectB, listofA);
        }
        listofA.add(objectA);
    }

    protected synchronized void addBToA(A objectA, B objectB) {
        UpdateableTreeSet<B> list = aToBMap.get(objectA);
        if (list == null) {
            list = new UpdateableTreeSet<B>();
            aToBMap.put(objectA, list);
        }
        list.add(objectB);
    }

    protected synchronized void decouple(final A objectA, final B objectB) {
        final Set<B> setOfB = aToBMap.get(objectA);
        if (setOfB != null) {
            setOfB.remove(objectB);
            if (setOfB.isEmpty()) {
                aToBMap.remove(objectA);
            }
        }
        final Set<A> setOfA = bToAMap.get(objectB);
        if (setOfA != null) {
            setOfA.remove(objectA);
            if (setOfA.isEmpty()) {
                bToAMap.remove(objectB);
            }
        }
    }

    protected synchronized Set<B> removeObjectA(final A objectA) {
        final Set<B> removedSet = aToBMap.remove(objectA);
        if (removedSet != null) {
            for (final B objectB : removedSet) {
                bToAMap.get(objectB).remove(objectA);
            }
        }
        return removedSet;
    }

    protected synchronized Set<A> removeObjectB(final B objectB) {
        final Set<A> removedSet = bToAMap.remove(objectB);
        if (removedSet != null) {
            for (final A objectA : removedSet) {
                aToBMap.get(objectA).remove(objectB);
            }
        }
        return removedSet;
    }

    protected synchronized UpdateableTreeSet<A> getAllAInB(final B objectB) {
        return bToAMap.get(objectB);
    }

    protected synchronized UpdateableTreeSet<B> getAllBInA(final A objectA) {
        return aToBMap.get(objectA);
    }
}