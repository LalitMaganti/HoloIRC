package com.fusionx.lightirc.collections;

import java.util.HashMap;
import java.util.Set;

import de.scrum_master.util.UpdateableTreeSet;

import static de.scrum_master.util.UpdateableTreeSet.Updateable;

public abstract class TwoWayHashSet<A extends Updateable, B extends Updateable> {
    protected final HashMap<A, UpdateableTreeSet<B>> aToBMap;
    protected final HashMap<B, UpdateableTreeSet<A>> bToAMap;

    public TwoWayHashSet() {
        aToBMap = new HashMap<>();
        bToAMap = new HashMap<>();
    }

    protected synchronized void couple(final A objectA, final B objectB) {
        addAToB(objectA, objectB);
        addBToA(objectA, objectB);
    }

    protected synchronized void addAToB(A objectA, B objectB) {
        UpdateableTreeSet<A> listofA;
        if ((listofA = bToAMap.get(objectB)) == null) {
            listofA = new UpdateableTreeSet<>();
            bToAMap.put(objectB, listofA);
        }
        listofA.add(objectA);
    }

    protected synchronized void addBToA(A objectA, B objectB) {
        UpdateableTreeSet<B> list;
        if ((list = aToBMap.get(objectA)) == null) {
            list = new UpdateableTreeSet<>();
            aToBMap.put(objectA, list);
        }
        list.add(objectB);
    }

    protected synchronized void decouple(final A objectA, final B objectB) {
        // Needs an inexpensive way to check whether the objects are coupled
        // or if the objects exist at all in the maps
        final Set<B> setOfB = aToBMap.get(objectA);
        setOfB.remove(objectB);
        if (setOfB.isEmpty()) {
            aToBMap.remove(objectA);
        }
        final Set<A> setOfA = bToAMap.get(objectB);
        setOfA.remove(objectA);
        if (setOfA.isEmpty()) {
            bToAMap.remove(objectB);
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