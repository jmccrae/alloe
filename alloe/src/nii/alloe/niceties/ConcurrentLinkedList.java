package nii.alloe.niceties;
import java.util.*;
import java.io.*;

/**
 * A modified version of LinkedList. This implementation however will not
 * throw ConcurrentModificationExceptions, instead it simply returns the
 * next/previous element based on the last one returned by the iterator.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ConcurrentLinkedList<E> extends AbstractSequentialList<E>
        implements Serializable, Cloneable {
    /**
     * The first element in the list.
     */
    transient Entry first;
    
    /**
     * The last element in the list.
     */
    transient Entry last;
    
    /**
     * The current length of the list.
     */
    transient int size = 0;
    
    /**
     * Class to represent an entry in the list. Holds a single element.
     */
    private final class Entry {
        /** The element in the list. */
        E data;
        
        /** The next list entry, null if this is last. */
        Entry next;
        
        /** The previous list entry, null if this is first. */
        Entry previous;
        
        /**
         * Construct an entry.
         * @param data the list element
         */
        Entry(E data) {
            this.data = data;
        }
    } // class Entry
    
    /**
     * Obtain the Entry at a given position in a list. This method of course
     * takes linear time, but it is intelligent enough to take the shorter of the
     * paths to get to the Entry required. This implies that the first or last
     * entry in the list is obtained in constant time, which is a very desirable
     * property.
     * For speed and flexibility, range checking is not done in this method:
     * Incorrect values will be returned if (n &lt; 0) or (n &gt;= size).
     *
     * @param n the number of the entry to get
     * @return the entry at position n
     */
    // Package visible for use in nested classes.
    Entry getEntry(int n) {
        Entry e;
        if (n < size / 2) {
            e = first;
            // n less than size/2, iterate from start
            while (n-- > 0)
                e = e.next;
        } else {
            e = last;
            // n greater than size/2, iterate from end
            while (++n < size)
                e = e.previous;
        }
        return e;
    }
    
    /**
     * Remove an entry from the list. This will adjust size and deal with
     *  `first' and  `last' appropriatly.
     *
     * @param e the entry to remove
     */
    // Package visible for use in nested classes.
    void removeEntry(Entry e) {
        modCount++;
        size--;
        if (size == 0)
            first = last = null;
        else {
            if (e == first) {
                first = e.next;
                e.next.previous = null;
            } else if (e == last) {
                last = e.previous;
                e.previous.next = null;
            } else {
                e.next.previous = e.previous;
                e.previous.next = e.next;
            }
        }
    }
    
    /**
     * Checks that the index is in the range of possible elements (inclusive).
     *
     * @param index the index to check
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size
     */
    private void checkBoundsInclusive(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size:"
                    + size);
    }
    
    /**
     * Checks that the index is in the range of existing elements (exclusive).
     *
     * @param index the index to check
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size
     */
    private void checkBoundsExclusive(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size:"
                    + size);
    }
    
    /**
     * Create an empty linked list.
     */
    public ConcurrentLinkedList() {
    }
    
    /**
     * Create a linked list containing the elements, in order, of a given
     * collection.
     *
     * @param c the collection to populate this list from
     * @throws NullPointerException if c is null
     */
    public ConcurrentLinkedList(Collection<? extends E> c) {
        addAll(c);
    }
    
    /**
     * Returns the first element in the list.
     *
     * @return the first list element
     * @throws NoSuchElementException if the list is empty
     */
    public E getFirst() {
        if (size == 0)
            throw new NoSuchElementException();
        return first.data;
    }
    
    /**
     * Returns the last element in the list.
     *
     * @return the last list element
     * @throws NoSuchElementException if the list is empty
     */
    public E getLast() {
        if (size == 0)
            throw new NoSuchElementException();
        return last.data;
    }
    
    /**
     * Remove and return the first element in the list.
     *
     * @return the former first element in the list
     * @throws NoSuchElementException if the list is empty
     */
    public E removeFirst() {
        if (size == 0)
            throw new NoSuchElementException();
        modCount++;
        size--;
        E r = first.data;
        
        if (first.next != null)
            first.next.previous = null;
        else
            last = null;
        
        first = first.next;
        
        return r;
    }
    
    /**
     * Remove and return the last element in the list.
     *
     * @return the former last element in the list
     * @throws NoSuchElementException if the list is empty
     */
    public E removeLast() {
        if (size == 0)
            throw new NoSuchElementException();
        modCount++;
        size--;
        E r = last.data;
        
        if (last.previous != null)
            last.previous.next = null;
        else
            first = null;
        
        last = last.previous;
        
        return r;
    }
    
    /**
     * Insert an element at the first of the list.
     *
     * @param o the element to insert
     */
    public void addFirst(E o) {
        Entry e = new Entry(o);
        
        modCount++;
        if (size == 0)
            first = last = e;
        else {
            e.next = first;
            first.previous = e;
            first = e;
        }
        size++;
    }
    
    /**
     * Insert an element at the last of the list.
     *
     * @param o the element to insert
     */
    public void addLast(E o) {
        addLastEntry(new Entry(o));
    }
    
    /**
     * Inserts an element at the end of the list.
     *
     * @param e the entry to add
     */
    private void addLastEntry(Entry e) {
        modCount++;
        if (size == 0)
            first = last = e;
        else {
            e.previous = last;
            last.next = e;
            last = e;
        }
        size++;
    }
    
    /**
     * Returns true if the list contains the given object. Comparison is done by
     * <code>o == null ? e = null : o.equals(e)</code>.
     *
     * @param o the element to look for
     * @return true if it is found
     */
    public boolean contains(Object o) {
        Entry e = first;
        while (e != null) {
            if (o.equals(e.data))
                return true;
            e = e.next;
        }
        return false;
    }
    
    /**
     * Returns the size of the list.
     *
     * @return the list size
     */
    public int size() {
        return size;
    }
    
    /**
     * Adds an element to the end of the list.
     *
     * @param e the entry to add
     * @return true, as it always succeeds
     */
    public boolean add(E o) {
        addLastEntry(new Entry(o));
        return true;
    }
    
    /**
     * Removes the entry at the lowest index in the list that matches the given
     * object, comparing by <code>o == null ? e = null : o.equals(e)</code>.
     *
     * @param o the object to remove
     * @return true if an instance of the object was removed
     */
    public boolean remove(Object o) {
        Entry e = first;
        while (e != null) {
            if (o.equals(e.data)) {
                removeEntry(e);
                return true;
            }
            e = e.next;
        }
        return false;
    }
    
    /**
     * Append the elements of the collection in iteration order to the end of
     * this list. If this list is modified externally (for example, if this
     * list is the collection), behavior is unspecified.
     *
     * @param c the collection to append
     * @return true if the list was modified
     * @throws NullPointerException if c is null
     */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
    
    /**
     * Insert the elements of the collection in iteration order at the given
     * index of this list. If this list is modified externally (for example,
     * if this list is the collection), behavior is unspecified.
     *
     * @param c the collection to append
     * @return true if the list was modified
     * @throws NullPointerException if c is null
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        checkBoundsInclusive(index);
        int csize = c.size();
        
        if (csize == 0)
            return false;
        
        Iterator<? extends E> itr = c.iterator();
        
        // Get the entries just before and after index. If index is at the start
        // of the list, BEFORE is null. If index is at the end of the list, AFTER
        // is null. If the list is empty, both are null.
        Entry after = null;
        Entry before = null;
        if (index != size) {
            after = getEntry(index);
            before = after.previous;
        } else
            before = last;
        
        // Create the first new entry. We do not yet set the link from `before'
        // to the first entry, in order to deal with the case where (c == this).
        // [Actually, we don't have to handle this case to fufill the
        // contract for addAll(), but Sun's implementation appears to.]
        Entry e = new Entry(itr.next());
        e.previous = before;
        Entry prev = e;
        Entry firstNew = e;
        
        // Create and link all the remaining entries.
        for (int pos = 1; pos < csize; pos++) {
            e = new Entry(itr.next());
            e.previous = prev;
            prev.next = e;
            prev = e;
        }
        
        // Link the new chain of entries into the list.
        modCount++;
        size += csize;
        prev.next = after;
        if (after != null)
            after.previous = e;
        else
            last = e;
        
        if (before != null)
            before.next = firstNew;
        else
            first = firstNew;
        return true;
    }
    
    /**
     * Remove all elements from this list.
     */
    public void clear() {
        if (size > 0) {
            modCount++;
            first = null;
            last = null;
            size = 0;
        }
    }
    
    /**
     * Return the element at index.
     *
     * @param index the place to look
     * @return the element at index
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public E get(int index) {
        checkBoundsExclusive(index);
        return getEntry(index).data;
    }
    
    /**
     * Replace the element at the given location in the list.
     *
     * @param index which index to change
     * @param o the new element
     * @return the prior element
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public E set(int index, E o) {
        checkBoundsExclusive(index);
        Entry e = getEntry(index);
        E old = e.data;
        e.data = o;
        return old;
    }
    
    /**
     * Inserts an element in the given position in the list.
     *
     * @param index where to insert the element
     * @param o the element to insert
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public void add(int index, E o) {
        checkBoundsInclusive(index);
        Entry e = new Entry(o);
        
        if (index < size) {
            modCount++;
            Entry after = getEntry(index);
            e.next = after;
            e.previous = after.previous;
            if (after.previous == null)
                first = e;
            else
                after.previous.next = e;
            after.previous = e;
            size++;
        } else
            addLastEntry(e);
    }
    
    /**
     * Removes the element at the given position from the list.
     *
     * @param index the location of the element to remove
     * @return the removed element
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public E remove(int index) {
        checkBoundsExclusive(index);
        Entry e = getEntry(index);
        removeEntry(e);
        return e.data;
    }
    
    /**
     * Returns the first index where the element is located in the list, or -1.
     *
     * @param o the element to look for
     * @return its position, or -1 if not found
     */
    public int indexOf(Object o) {
        int index = 0;
        Entry e = first;
        while (e != null) {
            if (o.equals(e.data))
                return index;
            index++;
            e = e.next;
        }
        return -1;
    }
    
    /**
     * Returns the last index where the element is located in the list, or -1.
     *
     * @param o the element to look for
     * @return its position, or -1 if not found
     */
    public int lastIndexOf(Object o) {
        int index = size - 1;
        Entry e = last;
        while (e != null) {
            if (o.equals(e.data))
                return index;
            index--;
            e = e.previous;
        }
        return -1;
    }
    
    /**
     * Obtain a ListIterator over this list, starting at a given index. The
     * ListIterator returned by this method supports the add, remove and set
     * methods.
     *
     * @param index the index of the element to be returned by the first call to
     *        next(), or size() to be initially positioned at the end of the list
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public ListIterator listIterator(int index) {
        checkBoundsInclusive(index);
        return new LinkedListItr(index);
    }
    
    /**
     * Create a shallow copy of this LinkedList (the elements are not cloned).
     *
     * @return an object of the same class as this object, containing the
     *         same elements in the same order
     */
    public Object clone() {
        LinkedList copy = null;
        try {
            copy = (LinkedList) super.clone();
        } catch (CloneNotSupportedException ex) {
        }
        copy.clear();
        copy.addAll(this);
        return copy;
    }
    
    /**
     * Returns an array which contains the elements of the list in order.
     *
     * @return an array containing the list elements
     */
    public E[] toArray() {
        Object[] array = new Object[size];
        Entry e = first;
        for (int i = 0; i < size; i++) {
            array[i] = e.data;
            e = e.next;
        }
        return (E[])array;
    }
    
    /**
     * Returns an Array whose component type is the runtime component type of
     * the passed-in Array.  The returned Array is populated with all of the
     * elements in this LinkedList.  If the passed-in Array is not large enough
     * to store all of the elements in this List, a new Array will be created
     * and returned; if the passed-in Array is <i>larger</i> than the size
     * of this List, then size() index will be set to null.
     *
     * @param a the passed-in Array
     * @return an array representation of this list
     * @throws ArrayStoreException if the runtime type of a does not allow
     *         an element in this list
     * @throws NullPointerException if a is null
     */
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        else if (a.length > size)
            a[size] = null;
        Entry e = first;
        for (int i = 0; i < size; i++) {
            a[i] = (T)e.data;
            e = e.next;
        }
        return a;
    }
    
    /**
     * Serializes this object to the given stream.
     *
     * @param s the stream to write to
     * @throws IOException if the underlying stream fails
     * @serialData the size of the list (int), followed by all the elements
     *             (Object) in proper order
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        Entry e = first;
        while (e != null) {
            s.writeObject(e.data);
            e = e.next;
        }
    }
    
    /**
     * Deserializes this object from the given stream.
     *
     * @param s the stream to read from
     * @throws ClassNotFoundException if the underlying stream fails
     * @throws IOException if the underlying stream fails
     * @serialData the size of the list (int), followed by all the elements
     *             (Object) in proper order
     */
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int i = s.readInt();
        while (--i >= 0)
            addLastEntry(new Entry((E)s.readObject()));
    }
    
    /**
     * A ListIterator over the list. This class keeps track of its
     * position in the list and the two list entries it is between.
     *
     * @author Original author unknown
     * @author Eric Blake <ebb9@email.byu.edu>
     */
    private final class LinkedListItr implements ListIterator<E>
    {
        /** Number of modifications we know about. */
        private int knownMod = modCount;
        
        /** Entry that will be returned by next(). */
        private Entry next;
        
        /** Entry that will be returned by previous(). */
        private Entry previous;
        
        /** Entry that will be affected by remove() or set(). */
        private Entry lastReturned;
        
        /** Index of `next'. */
        // private int position;
        
        /**
         * Initialize the iterator.
         *
         * @param index the initial index
         */
        LinkedListItr(int index) {
            if (index == size) {
                next = null;
                previous = last;
            } else {
                next = getEntry(index);
                previous = next.previous;
            }
            //position = index;
        }
        
        /**
         * Checks for iterator consistency and fixes any problems
         *
         */
        private void checkMod() {
            if (knownMod != modCount) {
                next = lastReturned.next;
                while(next != null && ((next.previous != null && next.previous.next != next) || next.previous == first)) {
                    next = next.next;
                }
                previous = lastReturned.previous;
                while(previous != null && ((previous.next != null && previous.next.previous != previous) || previous.next == first)) {
                    previous = previous.previous;
                }
                knownMod = modCount;
            }
        }
        
        /**
         * Returns the index of the next element.
         *
         * @return the next index
         */
        public int nextIndex() {
            checkMod();
            return indexOf(next);
        }
        
        /**
         * Returns the index of the previous element.
         *
         * @return the previous index
         */
        public int previousIndex() {
            checkMod();
            return indexOf(previous);
        }
        
        /**
         * Returns true if more elements exist via next.
         *
         * @return true if next will succeed
         */
        public boolean hasNext() {
            checkMod();
            return (next != null);
        }
        
        /**
         * Returns true if more elements exist via previous.
         *
         * @return true if previous will succeed
         */
        public boolean hasPrevious() {
            checkMod();
            return (previous != null);
        }
        
        /**
         * Returns the next element.
         *
         * @return the next element
         * @throws NoSuchElementException if there is no next
         */
        public E next() {
            checkMod();
            if (next == null)
                throw new NoSuchElementException();
            //position++;
            lastReturned = previous = next;
            next = lastReturned.next;
            return lastReturned.data;
        }
        
        /**
         * Returns the previous element.
         *
         * @return the previous element
         * @throws NoSuchElementException if there is no previous
         */
        public E previous() {
            checkMod();
            if (previous == null)
                throw new NoSuchElementException();
            //position--;
            lastReturned = next = previous;
            previous = lastReturned.previous;
            return lastReturned.data;
        }
        
        /**
         * Remove the most recently returned element from the list.
         *
         * @throws IllegalStateException if there was no last element, or an attempt has been made to removed an existing element
         */
        public void remove() {
            checkMod();
            if (lastReturned == null || (lastReturned.previous != null && lastReturned.previous.next != lastReturned))
                throw new IllegalStateException();
            
            // Adjust the position to before the removed element, if the element
            // being removed is behind the cursor.
            //if (lastReturned == previous)
            //position--;
            
            next = lastReturned.next;
            previous = lastReturned.previous;
            removeEntry(lastReturned);
            knownMod++;
            
            lastReturned = null;
        }
        
        /**
         * Adds an element between the previous and next, and advance to the next.
         *
         * @param o the element to add
         */
        public void add(E o) {
            checkMod();
            modCount++;
            knownMod++;
            size++;
            //position++;
            Entry e = new Entry(o);
            e.previous = previous;
            e.next = next;
            
            if (previous != null)
                previous.next = e;
            else
                first = e;
            
            if (next != null)
                next.previous = e;
            else
                last = e;
            
            previous = e;
            lastReturned = null;
        }
        
        /**
         * Changes the contents of the element most recently returned.
         *
         * @param o the new element
         * @throws IllegalStateException if there was no last element
         */
        public void set(E o) {
            checkMod();
            if (lastReturned == null)
                throw new IllegalStateException();
            lastReturned.data = o;
        }
    } // class LinkedListItr
    
}
