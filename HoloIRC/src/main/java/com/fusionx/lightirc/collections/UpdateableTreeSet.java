package com.fusionx.lightirc.collections;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is just a thin layer around its parent class, adding an element updating and
 * re-sorting feature which works as follows: <p/> As you might know you should never add or remove
 * elements of Java collections while iterating over them in a loop. There is one exception: If you
 * use an {@link java.util.Iterator Iterator} you may safely use its {@link
 * java.util.Iterator#remove() remove} method. Many people do not know that or are even unaware of
 * that method's existence because they iterate using {@code for} loops. <p/> Another problem
 * specifically with sorted collections is that even if they are {@link Comparable Comparable} or
 * use an explicit {@link java.util.Comparator Comparator}, their elements will be sorted at
 * <b>insertion time</b> only and the sort order not updated even if their sorting keys change. If
 * you want to achieve a refresh, you have to <ol> <li> remove them from the collection, <li> update
 * their values and then <li> add them back again. </ol> This class does just that, but in a
 * structured fashion as a bulk operation <b>after</b> your loop has finished or as a single-element
 * operation if you want to do it outside a loop. <p/> Usage example:
 * <pre>
 * import de.scrum_master.util.UpdateableTreeSet;
 * import de.scrum_master.util.UpdateableTreeSet.Updateable;
 *
 * class MyType implements Updateable {
 *     void update(Object newValue) {
 *         // Change the receiver's value
 *     }
 * }
 *
 * SortedSet<MyType> mySortedSet = new UpdateableTreeSet<MyType>();
 *
 * // Add elements to mySortedSet...
 *
 * for (MyType element : mySortedSet) {
 *     if (removeCondition)
 *         markForRemoval(element);
 *     if (updateCondition)
 *         markForUpdate(element, newValue);
 * }
 *
 * mySortedSet.updateMarked();
 * </pre>
 * Special thanks go to user <a href="http://stackoverflow.com/users/15472/tucuxi">tucuxi at
 * stackoverflow.com</a> because his <a href="http://stackoverflow.com/a/2581450/1082681">answer</a>
 * in one of the discussion threads inspired me to implement this class based on his idea. My
 * version is just a bit more sophisticated because it supports deferred updates and is thus useful
 * within {@code for} loops. <p/>
 *
 * @author Alexander Kriegisch, <a href="http://scrum-master.de">Scrum-Master.de</a>
 */
public class UpdateableTreeSet<E extends UpdateableTreeSet.Updateable> extends TreeSet<E> {

    /**
     * UpdateableTreeSet elements must implement this interface in order to provide a structured way
     * of updating themselves at the right moment during an update operation, i.e. after temporary
     * removal from the collection and before being added back into the collection.
     *
     * @author Alexander Kriegisch, <a href="http://scrum-master.de">Scrum-Master.de</a>
     */
    public interface Updateable {

        /**
         * Update method for elements which do not need to be given new values, but have a way of
         * updating themselves in another way. If you do not need this method, just specify a
         * version calling {@link #update(Object)} with a null value.
         */
        void update();

        /**
         * Update method for elements which need one or more new values. If more than one value is
         * necessary, {@code newValue} could e.g. be a Map or a List. If you do not need this
         * method, just specify a version dropping the value and calling {@link #update()} instead.
         */
        void update(Object newValue);
    }

    private static final long serialVersionUID = 1170156554123865966L;

    private final Set<E> toBeAdded = new HashSet<E>();

    UpdateableTreeSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    /**
     * Mark an element for subsequent addition by {@link #addMarked()}. <p/> <b>Attention:</b>
     * Beware of manually modifying a marked element before its scheduled update. It might not be
     * found anymore (and thus not removed) because of its changed key, which later could lead to
     * strange double entries in the collection.
     */
    public void markForAddition(E element) {
        toBeAdded.add(element);
    }

    /**
     * Performs <ol> <li> a bulk removal on all elements previously marked for removal, <li> a bulk
     * update on all elements previously marked for update so as to trigger their re-sorting within
     * the collection. </ol> The marks will be removed from the processed elements after the bulk
     * operation, so you can start to mark other elements afterwards. <p/> Please note that if any
     * remove or update action fails, this will be silently ignored as long as there are no
     * exceptions. <p/> <b>Attention:</b> Do not call this method while looping over the collection
     * and beware of manually modifying any marked elements before their scheduled update/removal.
     * They might not be found anymore (and thus not removed) because of their changed keys, which
     * later could lead to strange double entries in the collection.
     */
    public synchronized void addMarked() {
        addAll(toBeAdded);
        toBeAdded.clear();
    }

    /**
     * Performs an immediate update on a single element so as to trigger its re-ordering within the
     * collection. Calling this method has no effect on the list of elements marked for removal or
     * update. <ol> <b>Attention:</b> Do not call this method while looping over the collection.
     *
     * @param element  the element to be updated
     * @param newValue element's new value (if any)
     * @return true if element was found in collection and updated (i.e. removed and added back)
     * successfully
     */
    public synchronized boolean update(E element, Object newValue) {
        if (remove(element)) {
            element.update(newValue);
            return add(element);
        }
        return false;
    }
}